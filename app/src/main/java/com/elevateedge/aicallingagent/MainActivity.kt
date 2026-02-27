package com.elevateedge.aicallingagent

import android.content.Intent

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


import androidx.recyclerview.widget.LinearLayoutManager
import com.elevateedge.aicallingagent.data.AppDatabase
import com.elevateedge.aicallingagent.data.LeadRepository
import com.elevateedge.aicallingagent.data.SettingsManager
import com.elevateedge.aicallingagent.databinding.ActivityMainBinding
import com.elevateedge.aicallingagent.ui.LeadAdapter
import com.elevateedge.aicallingagent.ui.LeadViewModel
import com.elevateedge.aicallingagent.ui.LeadViewModelFactory
import com.elevateedge.aicallingagent.utils.CsvParser
import com.elevateedge.aicallingagent.utils.AudioPlayer
import com.elevateedge.aicallingagent.services.CallForegroundService


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy { LeadRepository(database.leadDao()) }
    private val settingsManager by lazy { SettingsManager(this) }
    private val viewModel: LeadViewModel by viewModels {
        LeadViewModelFactory(repository)
    }

    private val csvPickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { importCsv(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = LeadAdapter { lead ->
            lead.notes?.let { path ->
                AudioPlayer.playAudio(path)
                Toast.makeText(this, "Playing recording for ${lead.businessName}", Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(this, "No recording available", Toast.LENGTH_SHORT).show()
            }
        }
        binding.leadRecyclerView.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        viewModel.allLeads.observe(this) { leads ->
            adapter.submitList(leads)
        }

        binding.importLeadsBtn.setOnClickListener {
            csvPickerLauncher.launch("text/comma-separated-values")
        }

        binding.settingsBtn.setOnClickListener {
            showSettingsDialog()
        }

        binding.startSessionBtn.setOnClickListener {
            if (checkPermissions()) {
                val intent = Intent(this, CallForegroundService::class.java).apply {
                    putExtra("LEAD_ID", -2L) // Signal to start with first pending lead
                }
                startForegroundService(intent)
                Toast.makeText(this, "Starting call session...", Toast.LENGTH_SHORT).show()
            } else {
                requestPermissions()
            }
        }
    }

    private fun importCsv(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val leads = CsvParser.parseLeads(inputStream)
                viewModel.insertLeads(leads)
                Toast.makeText(this, "Imported ${leads.size} leads", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error importing CSV: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun checkPermissions(): Boolean {
        val permissions = arrayOf(
            Manifest.permission.CALL_PHONE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE
        )
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.CALL_PHONE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_PHONE_STATE
            ),
            123
        )
    }

    private fun showSettingsDialog() {
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(60, 20, 60, 20)
        }

        val apiKeyInput = android.widget.EditText(this).apply {
            hint = "Vapi API Key"
            inputType = android.text.InputType.TYPE_CLASS_TEXT
        }
        val assistantIdInput = android.widget.EditText(this).apply {
            hint = "Vapi Assistant ID (Optional)"
        }
        val phoneNumberIdInput = android.widget.EditText(this).apply {
            hint = "Vapi Phone Number ID (Outbound)"
        }

        layout.addView(apiKeyInput)
        layout.addView(assistantIdInput)
        layout.addView(phoneNumberIdInput)

        CoroutineScope(Dispatchers.Main).launch {
            apiKeyInput.setText(settingsManager.vapiApiKey.first())
            assistantIdInput.setText(settingsManager.vapiAssistantId.first())
            phoneNumberIdInput.setText(settingsManager.vapiPhoneNumberId.first())
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("AI Calling Settings")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val apiKey = apiKeyInput.text.toString()
                val assistantId = assistantIdInput.text.toString()
                val phoneNumberId = phoneNumberIdInput.text.toString()
                CoroutineScope(Dispatchers.IO).launch {
                    settingsManager.saveVapiSettings(apiKey, assistantId, phoneNumberId)
                }
                Toast.makeText(this, "Settings saved. Now internet calls will be used.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
