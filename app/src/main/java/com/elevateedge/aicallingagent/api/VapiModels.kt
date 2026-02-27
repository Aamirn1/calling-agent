package com.elevateedge.aicallingagent.api

import com.google.gson.annotations.SerializedName

data class VapiCallRequest(
    @SerializedName("phoneNumberId") val phoneNumberId: String?,
    @SerializedName("customer") val customer: VapiCustomer,
    @SerializedName("assistantId") val assistantId: String?,
    @SerializedName("assistant") val assistant: VapiAssistant? = null
)

data class VapiCustomer(
    @SerializedName("number") val number: String,
    @SerializedName("name") val name: String? = null
)

data class VapiAssistant(
    @SerializedName("model") val model: VapiModel,
    @SerializedName("voice") val voice: VapiVoice,
    @SerializedName("firstMessage") val firstMessage: String
)

data class VapiModel(
    @SerializedName("provider") val provider: String = "openai",
    @SerializedName("model") val model: String = "gpt-3.5-turbo",
    @SerializedName("messages") val messages: List<VapiMessage>
)

data class VapiMessage(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String
)

data class VapiVoice(
    @SerializedName("provider") val provider: String = "11labs",
    @SerializedName("voiceId") val voiceId: String = "paula"
)

data class VapiCallResponse(
    @SerializedName("id") val id: String,
    @SerializedName("status") val status: String
)
