package com.elevateedge.aicallingagent.utils

import com.elevateedge.aicallingagent.data.Lead

object ScriptGenerator {
    private const val AGENCY_NAME = "Elevate Edge Digital Agency"
    private const val MOTTO = "Double Your Business Growth"
    
    fun generatePitch(lead: Lead): String {
        return "Hello, is this ${lead.businessName}? My name is Alex from $AGENCY_NAME. " +
               "We focus on helping small businesses like yours $MOTTO through affordable web design, " +
               "digital ads, and social media management. I noticed your business has great potential. " +
               "Would you like to discuss how we can help your business grow?"
    }
    
    fun getVoicemailMessage(): String {
        return "Hello, this is $AGENCY_NAME. We help businesses $MOTTO with affordable digital services. " +
               "Please call us back if you're interested in growing your online presence. Have a great day."
    }
}
