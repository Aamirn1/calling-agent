package com.elevateedge.aicallingagent.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import java.util.Locale

class TtsManager(context: Context, onInitSuccess: () -> Unit) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                tts?.language = Locale.US
                onInitSuccess()
            } else {
                Log.e("TtsManager", "Init failed")
            }
        }
    }

    fun speak(text: String) {
        if (isInitialized) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "UtteranceId")
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
    }

    fun getAvailableVoices(): List<Voice> {
        return tts?.voices?.toList() ?: emptyList()
    }

    fun setVoice(voice: Voice) {
        tts?.voice = voice
    }
    
    fun setPitch(pitch: Float) {
        tts?.setPitch(pitch)
    }

    fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate)
    }
}
