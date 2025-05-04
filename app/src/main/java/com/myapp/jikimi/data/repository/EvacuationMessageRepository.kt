package com.myapp.jikimi.data.repository

import com.myapp.jikimi.Resource
import com.myapp.jikimi.data.model.dto.EvacuationMessage

interface EvacuationMessageRepository {
    suspend fun getLatestEvacuationMessage(date: String): Resource<EvacuationMessage>
}