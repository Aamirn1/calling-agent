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
import com.elevateedge.aicallingagent.utils.ScriptGenerator
import com.elevateedge.aicallingagent.utils.TtsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.elevateedge.aicallingagent.utils.CallRecorder
import com.elevateedge.aicallingagent.services.CallReceiver


class CallForegroundService : Service() {
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private lateinit var ttsManager: TtsManager
    private lateinit var repository: LeadRepository
    private lateinit var callRecorder: CallRecorder
    private var currentLead: Lead? = null
    private var callReceiver: CallReceiver? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        repository = LeadRepository(AppDatabase.getDatabase(this).leadDao())
        ttsManager = TtsManager(this) {
            // Initialized
        }
        callRecorder = CallRecorder(getExternalFilesDir(null)!!)
        
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
                }
            }
            TelephonyManager.EXTRA_STATE_IDLE -> {
                // Call ended
                val path = callRecorder.stopRecording()
                // Logic to update lead status and notes with recording path
                stopSelf()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val leadId = intent?.getLongExtra("LEAD_ID", -1L) ?: -1L
        if (leadId != -1L) {
            // Logic to fetch lead and start call would go here
        }
        
        val notification = createNotification("Calling lead...")
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
