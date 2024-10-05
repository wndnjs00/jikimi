package com.example.jikimi.data.repository

import com.example.jikimi.data.model.dto.NaturalDisasterResponse

interface NaturalDisasterRepository {
    suspend fun requestNaturalDisaster(safetyCate : String) : NaturalDisasterResponse
}