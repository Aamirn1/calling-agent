package com.elevateedge.aicallingagent.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "leads")
data class Lead(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val businessName: String,
    val phoneNumber: String,
    val status: String = "Pending", // Pending, Called, Interested, Not Interested, No Answer, Busy
    val lastCalled: Long? = null,
    val notes: String? = null
)
