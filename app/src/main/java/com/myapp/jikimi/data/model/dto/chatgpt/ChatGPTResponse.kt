package com.myapp.jikimi.data.model.dto.chatgpt

data class ChatGPTResponse(
    val id: String = "",
    val `object`: String = "",
    val created: Long,
    val model: String = "",
    val choices: List<Choice> = emptyList(),
    val usage: Usage,
)

data class Choice(
    val index: Int,
    val message: Message,
    val finish_reason: String = "",
)

data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)