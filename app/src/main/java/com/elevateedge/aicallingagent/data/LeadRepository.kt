package com.elevateedge.aicallingagent.data

import kotlinx.coroutines.flow.Flow

class LeadRepository(private val leadDao: LeadDao) {

    val allLeads: Flow<List<Lead>> = leadDao.getAllLeads()

    suspend fun insertLead(lead: Lead) {
        leadDao.insertLead(lead)
    }

    suspend fun insertLeads(leads: List<Lead>) {
        leadDao.insertLeads(leads)
    }

    suspend fun updateLead(lead: Lead) {
        leadDao.updateLead(lead)
    }

    suspend fun deleteLead(lead: Lead) {
        leadDao.deleteLead(lead)
    }

    suspend fun deleteAllLeads() {
        leadDao.deleteAllLeads()
    }
}
