package com.example.jikimi.data.network.service

import com.example.jikimi.data.model.dto.NaturalDisasterResponse
import com.example.jikimi.data.network.NATURALDISASTER_API
import com.example.jikimi.data.network.NATURALDISASTER_SERVICE_KEY
import retrofit2.http.GET
import retrofit2.http.Query

interface NaturalDisasterService {
    @GET(NATURALDISASTER_API)
    suspend fun getNaturalDisaster(
        @Query("serviceKey") serviceKey : String = NATURALDISASTER_SERVICE_KEY,
        @Query("safety_cate") safetyCate : String,
    ) : NaturalDisasterResponse
}