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
                val part1 = tokens[0].trim().removeSurrounding("\"")
                val part2 = tokens[1].trim().removeSurrounding("\"")
                
                // If part1 starts with + or is digits, assume it's the phone number
                val (phoneNumber, businessName) = if (part1.startsWith("+") || part1.any { it.isDigit() } && !part2.any { it.isDigit() }) {
                    part1 to part2
                } else {
                    part2 to part1
                }

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
