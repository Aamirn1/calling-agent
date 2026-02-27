package com.elevateedge.aicallingagent.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface VapiService {
    @POST("call/phone")
    suspend fun createCall(
        @Header("Authorization") token: String,
        @Body request: VapiCallRequest
    ): Response<VapiCallResponse>
}
