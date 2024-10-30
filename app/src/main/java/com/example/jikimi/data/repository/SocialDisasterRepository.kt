package com.example.jikimi.data.repository

import com.example.jikimi.data.model.dto.SocialDisasterResponse

interface SocialDisasterRepository {
    suspend fun requestSocialDisaster(safetyCate: String) : SocialDisasterResponse
}