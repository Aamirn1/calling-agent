package com.elevateedge.aicallingagent.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

object CallUtils {
    fun makeCall(context: Context, phoneNumber: String) {
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$phoneNumber")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
