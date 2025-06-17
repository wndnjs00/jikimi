package com.myapp.jikimi.data.network.service

import com.myapp.jikimi.data.model.dto.chatgpt.ChatGPTRequest
import com.myapp.jikimi.data.model.dto.chatgpt.ChatGPTResponse
import com.myapp.jikimi.data.network.CHATGPT_API
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface ChatGPTApiService {
    @Headers("Content-Type: application/json")
    @POST(CHATGPT_API)
    suspend fun getChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: ChatGPTRequest
    ): Response<ChatGPTResponse>
}