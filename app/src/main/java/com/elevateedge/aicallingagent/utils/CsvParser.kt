package com.elevateedge.aicallingagent.utils

import com.elevateedge.aicallingagent.data.Lead
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

object CsvParser {
    fun parseLeads(inputStream: InputStream): List<Lead> {
        val leads = mutableListOf<Lead>()
        val reader = BufferedReader(InputStreamReader(inputStream))
        
        // Skip header if exists (Business Name, Phone Number)
        val firstLine = reader.readLine()
        
        var line: String? = reader.readLine()
        while (line != null) {
            val tokens = line.split(",")
            if (tokens.size >= 2) {
                val businessName = tokens[0].trim().removeSurrounding("\"")
                val phoneNumber = tokens[1].trim().removeSurrounding("\"")
                if (businessName.isNotEmpty() && phoneNumber.isNotEmpty()) {
                    leads.add(Lead(businessName = businessName, phoneNumber = phoneNumber))
                }
            }
            line = reader.readLine()
        }
        reader.close()
        return leads
    }
}
