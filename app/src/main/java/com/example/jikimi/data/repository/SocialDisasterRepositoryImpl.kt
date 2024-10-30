package com.example.jikimi.data.repository

import com.example.jikimi.data.model.dto.SocialDisasterResponse
import com.example.jikimi.data.network.service.SocialDisasterService
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class SocialDisasterRepositoryImpl @Inject constructor(
    @Named("SocialDisasterService") private val socialDisasterService: SocialDisasterService
): SocialDisasterRepository{

    override suspend fun requestSocialDisaster(safetyCate: String): SocialDisasterResponse {
        return socialDisasterService.getSocialDisaster(safetyCate = safetyCate)
    }
}