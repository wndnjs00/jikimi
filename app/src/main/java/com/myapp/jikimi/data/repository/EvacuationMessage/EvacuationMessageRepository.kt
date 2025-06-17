package com.myapp.jikimi.data.repository.EvacuationMessage

import com.myapp.jikimi.Resource
import com.myapp.jikimi.data.model.dto.EvacuationMessage

interface EvacuationMessageRepository {
    suspend fun getLatestEvacuationMessage(date: String): Resource<EvacuationMessage>
}