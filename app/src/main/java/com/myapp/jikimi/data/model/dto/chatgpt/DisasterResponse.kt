package com.myapp.jikimi.data.model.dto.chatgpt

data class DisasterResponse(
    val title: String = "",
    val subtitle: String = "",
    val category: String = "",
    val riskLevel: String = "",
    val detailedSteps: List<String> = emptyList(),
)
