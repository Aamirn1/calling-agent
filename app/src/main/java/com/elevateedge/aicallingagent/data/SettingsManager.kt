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
        val KEY_VAPI_API_KEY = stringPreferencesKey("vapi_api_key")
        val KEY_VAPI_ASSISTANT_ID = stringPreferencesKey("vapi_assistant_id")
        val KEY_VAPI_PHONE_NUMBER_ID = stringPreferencesKey("vapi_phone_number_id")
    }

    val vapiApiKey: Flow<String?> = dataStore.data.map { it[KEY_VAPI_API_KEY] }
    val vapiAssistantId: Flow<String?> = dataStore.data.map { it[KEY_VAPI_ASSISTANT_ID] }
    val vapiPhoneNumberId: Flow<String?> = dataStore.data.map { it[KEY_VAPI_PHONE_NUMBER_ID] }

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

    suspend fun saveVapiSettings(apiKey: String, assistantId: String?, phoneNumberId: String?) {
        dataStore.edit {
            it[KEY_VAPI_API_KEY] = apiKey
            it[KEY_VAPI_ASSISTANT_ID] = assistantId ?: ""
            it[KEY_VAPI_PHONE_NUMBER_ID] = phoneNumberId ?: ""
        }
    }
}
