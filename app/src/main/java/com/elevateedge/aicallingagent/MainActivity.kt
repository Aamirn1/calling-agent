package com.elevateedge.aicallingagent

import android.content.Intent

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.elevateedge.aicallingagent.data.AppDatabase
import com.elevateedge.aicallingagent.data.LeadRepository
import com.elevateedge.aicallingagent.databinding.ActivityMainBinding
import com.elevateedge.aicallingagent.ui.LeadAdapter
import com.elevateedge.aicallingagent.ui.LeadViewModel
import com.elevateedge.aicallingagent.ui.LeadViewModelFactory
import com.elevateedge.aicallingagent.utils.CsvParser
import com.elevateedge.aicallingagent.utils.AudioPlayer


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy { LeadRepository(database.leadDao()) }
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

        binding.startSessionBtn.setOnClickListener {
            // Future implementation for starting call session
            Toast.makeText(this, "Starting call session...", Toast.LENGTH_SHORT).show()
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
}
