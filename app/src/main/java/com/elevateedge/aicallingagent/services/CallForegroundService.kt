package com.elevateedge.aicallingagent.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat

import android.content.IntentFilter
import android.telephony.TelephonyManager
import com.elevateedge.aicallingagent.data.AppDatabase
import com.elevateedge.aicallingagent.data.Lead
import com.elevateedge.aicallingagent.data.LeadRepository
import com.elevateedge.aicallingagent.utils.CallUtils
import com.elevateedge.aicallingagent.utils.ScriptGenerator
import com.elevateedge.aicallingagent.utils.TtsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.elevateedge.aicallingagent.utils.CallRecorder
import com.elevateedge.aicallingagent.services.CallReceiver
import com.elevateedge.aicallingagent.data.SettingsManager
import com.elevateedge.aicallingagent.api.VapiManager
import kotlinx.coroutines.flow.first


class CallForegroundService : Service() {
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private lateinit var ttsManager: TtsManager
    private lateinit var repository: LeadRepository
    private lateinit var callRecorder: CallRecorder
    private lateinit var settingsManager: SettingsManager
    private var currentLead: Lead? = null
    private var callReceiver: CallReceiver? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        repository = LeadRepository(AppDatabase.getDatabase(this).leadDao())
        ttsManager = TtsManager(this) { }
        callRecorder = CallRecorder(getExternalFilesDir(null)!!)
        settingsManager = SettingsManager(this)
        
        callReceiver = CallReceiver { state ->
            handleCallState(state)
        }
        registerReceiver(callReceiver, IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED))
    }

    private fun handleCallState(state: String) {
        when (state) {
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                // Call answered
                currentLead?.let { lead ->
                    callRecorder.startRecording(lead.id)
                    val pitch = ScriptGenerator.generatePitch(lead)
                    ttsManager.speak(pitch)
                    
                    // Update status to Called
                    serviceScope.launch {
                        repository.updateLead(lead.copy(status = "Called"))
                    }
                }
            }
            TelephonyManager.EXTRA_STATE_IDLE -> {
                // Call ended
                val path = callRecorder.stopRecording()
                currentLead?.let { lead ->
                    serviceScope.launch {
                        repository.updateLead(lead.copy(status = "Called", notes = path))
                        
                        // Wait a bit before next call
                        kotlinx.coroutines.delay(2000)
                        
                        // Start next call
                        val nextLead = repository.getFirstPendingLead()
                        if (nextLead != null) {
                            val nextIntent = Intent(this@CallForegroundService, CallForegroundService::class.java).apply {
                                putExtra("LEAD_ID", nextLead.id)
                            }
                            startService(nextIntent)
                        } else {
                            stopSelf()
                        }
                    }
                } ?: stopSelf()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val leadId = intent?.getLongExtra("LEAD_ID", -1L) ?: -1L
        if (leadId != -1L) {
            serviceScope.launch {
                val leadToCall = repository.getFirstPendingLead()
                
                if (leadToCall != null) {
                    currentLead = leadToCall
                    
                    val apiKey = settingsManager.vapiApiKey.first()
                    val assistantId = settingsManager.vapiAssistantId.first()
                    
                    if (!apiKey.isNullOrBlank()) {
                        // USE VAPI (INTERNET CALL)
                        val vapiManager = VapiManager(apiKey, if (assistantId.isNullOrBlank()) null else assistantId)
                        val result = vapiManager.startCall(leadToCall)
                        
                        if (result.isSuccess) {
                            // API call initiated. Vapi handles the VOIP part.
                            // We wait for user hangup or API status (simplified here: mark as called)
                            repository.updateLead(leadToCall.copy(status = "Calling (AI)"))
                            // Sequential logic will repeat when onStartCommand is called again or service loops
                            // For Vapi, we might need a webhook for completion, but for now we signal success.
                        } else {
                            // Fallback or error
                            repository.updateLead(leadToCall.copy(status = "Error: ${result.exceptionOrNull()?.message}"))
                            stopSelf()
                        }
                    } else {
                        // USE SIM (TRADITIONAL CALL)
                        CallUtils.makeCall(this@CallForegroundService, leadToCall.phoneNumber)
                    }
                } else {
                    stopSelf()
                }
            }
        }
        
        val notification = createNotification("AI Calling Agent Session Active")
        startForeground(1, notification)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(callReceiver)
        ttsManager.release()
        serviceJob.cancel()
    }
    private fun createNotification(content: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AI Calling Agent")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "AI Calling Agent Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "CallServiceChannel"
    }
}
