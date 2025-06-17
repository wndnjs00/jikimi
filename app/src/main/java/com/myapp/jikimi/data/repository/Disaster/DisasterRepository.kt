package com.myapp.jikimi.data.repository.Disaster

import com.myapp.jikimi.Resource
import com.myapp.jikimi.data.model.dto.chatgpt.DisasterResponse

interface DisasterRepository {
    suspend fun getTodayDisasterTips(): Resource<List<DisasterResponse>>
    suspend fun searchDisasterTips(query: String): Resource<DisasterResponse>
    suspend fun validateDisasterKeyword(query: String): Resource<Boolean>
}