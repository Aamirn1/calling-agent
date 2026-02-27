package com.elevateedge.aicallingagent.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsManager(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val KEY_VOICE_NAME = stringPreferencesKey("voice_name")
        val KEY_SPEECH_RATE = floatPreferencesKey("speech_rate")
        val KEY_PITCH = floatPreferencesKey("pitch")
    }

    val voiceName: Flow<String?> = dataStore.data.map { it[KEY_VOICE_NAME] }
    val speechRate: Flow<Float> = dataStore.data.map { it[KEY_SPEECH_RATE] ?: 1.0f }
    val pitch: Flow<Float> = dataStore.data.map { it[KEY_PITCH] ?: 1.0f }

    suspend fun saveSettings(voiceName: String, rate: Float, pitch: Float) {
        dataStore.edit {
            it[KEY_VOICE_NAME] = voiceName
            it[KEY_SPEECH_RATE] = rate
            it[KEY_PITCH] = pitch
        }
    }
}
