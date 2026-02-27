package com.elevateedge.aicallingagent.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.elevateedge.aicallingagent.data.Lead
import com.elevateedge.aicallingagent.data.LeadRepository
import kotlinx.coroutines.launch

class LeadViewModel(private val repository: LeadRepository) : ViewModel() {

    val allLeads = repository.allLeads.asLiveData()

    fun insertLeads(leads: List<Lead>) = viewModelScope.launch {
        repository.insertLeads(leads)
    }

    fun updateLead(lead: Lead) = viewModelScope.launch {
        repository.updateLead(lead)
    }

    fun deleteLead(lead: Lead) = viewModelScope.launch {
        repository.deleteLead(lead)
    }
}

class LeadViewModelFactory(private val repository: LeadRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LeadViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LeadViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
