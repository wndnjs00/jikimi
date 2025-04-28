package com.example.jikimi.data.repository

import com.example.jikimi.Resource
import com.example.jikimi.data.model.dto.EvacuationMessage

interface EvacuationMessageRepository {
    suspend fun getLatestEvacuationMessage(date: String): Resource<EvacuationMessage>
}