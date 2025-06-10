package com.myapp.jikimi.data.repository.Disaster

import com.myapp.jikimi.data.model.dto.chatgpt.DisasterResponse

interface DisasterRepository {
    suspend fun getTodayDisasterTips(): Result<List<DisasterResponse>>
    suspend fun searchDisasterTips(query: String): Result<DisasterResponse>
}