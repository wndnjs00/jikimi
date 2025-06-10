package com.myapp.jikimi.data.network.service

import com.myapp.jikimi.data.model.dto.chatgpt.ChatGPTRequest
import com.myapp.jikimi.data.model.dto.chatgpt.ChatGPTResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface ChatGPTApiService {
    @Headers("Content-Type: application/json")
    @POST("chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: ChatGPTRequest
    ): Response<ChatGPTResponse>
}