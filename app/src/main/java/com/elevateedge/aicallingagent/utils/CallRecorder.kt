package com.elevateedge.aicallingagent.utils

import android.media.MediaRecorder
import android.util.Log
import java.io.File
import java.io.IOException

class CallRecorder(private val outputDir: File) {
    private var mediaRecorder: MediaRecorder? = null
    private var currentFile: File? = null

    fun startRecording(leadId: Long) {
        val fileName = "call_${leadId}_${System.currentTimeMillis()}.amr"
        currentFile = File(outputDir, fileName)
        
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
            setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(currentFile?.absolutePath)
            
            try {
                prepare()
                start()
            } catch (e: IOException) {
                Log.e("CallRecorder", "prepare() failed: ${e.message}")
            } catch (e: IllegalStateException) {
                Log.e("CallRecorder", "start() failed: ${e.message}")
            }
        }
    }

    fun stopRecording(): String? {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.e("CallRecorder", "Stop failed: ${e.message}")
        }
        mediaRecorder = null
        return currentFile?.absolutePath
    }
}
