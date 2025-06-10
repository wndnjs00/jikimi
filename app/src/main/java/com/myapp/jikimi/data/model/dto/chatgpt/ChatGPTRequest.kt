package com.myapp.jikimi.data.model.dto.chatgpt

data class ChatGPTRequest(
    val model: String = "gpt-3.5-turbo",
    val messages: List<Message>,
    val max_tokens: Int = 1000,
    val temperature: Double = 0.7
)

data class Message(
    val role: String = "",
    val content: String = "",
)