package com.example.jikimi.data.network.service

import com.example.jikimi.data.model.dto.SocialDisasterResponse
import retrofit2.http.Query
import com.example.jikimi.data.network.SOCIAL_DISASTER_API
import com.example.jikimi.data.network.SOCIAL_DISASTER_SERVICE_KEY
import retrofit2.http.GET

interface SocialDisasterService {
    @GET(SOCIAL_DISASTER_API)
    suspend fun getSocialDisaster(
        @Query("serviceKey", encoded = true) serviceKey : String = SOCIAL_DISASTER_SERVICE_KEY,
        @Query("safety_cate") safetyCate : String,
    ) : SocialDisasterResponse
}