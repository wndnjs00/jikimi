package com.example.jikimi.data.network

import com.example.jikimi.data.model.dto.EarthquakeIndoorsShelterResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface IndoorEvacuationService {
    @GET(INDOOR_EVACUATION_API)

    suspend fun getIndoorEvacuation(
        @Query("serviceKey") serviceKey : String = INDOOR_EVACUATION_SERVICE_KEY,
        @Query("pageNo") pageNo : String = "3",             // 페이지번호 (대구-3적당)
        @Query("numOfRows") numOfRows : String = "200",      // 한페이지 결과수 (대구-200적당)
        @Query("type") type : String = "JSON", // 호출문서형식
    ) : EarthquakeIndoorsShelterResponse
}