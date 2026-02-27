package com.elevateedge.aicallingagent.api

import com.elevateedge.aicallingagent.data.Lead
import com.elevateedge.aicallingagent.utils.ScriptGenerator
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class VapiManager(private val apiKey: String, private val assistantId: String?, private val phoneNumberId: String?) {
    
    private val service: VapiService by lazy {
        val logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl("https://api.vapi.ai/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(VapiService::class.java)
    }

    suspend fun startCall(lead: Lead): Result<VapiCallResponse> {
        return try {
            val script = ScriptGenerator.generatePitch(lead)
            
            val request = VapiCallRequest(
                phoneNumberId = if (phoneNumberId.isNullOrBlank()) null else phoneNumberId,
                customer = VapiCustomer(number = lead.phoneNumber, name = lead.businessName),
                assistantId = assistantId,
                assistant = if (assistantId == null) VapiAssistant(
                    model = VapiModel(messages = listOf(VapiMessage("system", "You are a professional salesperson for Elevate Edge Digital Agency. Your motto is 'Double Your Business Growth'. Pitch $script"))),
                    voice = VapiVoice(),
                    firstMessage = script
                ) else null
            )

            val response = service.createCall("Bearer $apiKey", request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("API Error: ${response.code()} ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
