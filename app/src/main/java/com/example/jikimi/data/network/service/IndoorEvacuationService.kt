package com.example.jikimi.data.network.service

import com.example.jikimi.data.model.dto.EarthquakeIndoorsShelterResponse
import com.example.jikimi.data.network.INDOOR_EVACUATION_API
import com.example.jikimi.data.network.INDOOR_EVACUATION_SERVICE_KEY
import retrofit2.http.GET
import retrofit2.http.Query

// 행정안전부_지진겸용 임시주거시설 API
interface IndoorEvacuationService {
    @GET(INDOOR_EVACUATION_API)

    suspend fun getIndoorEvacuation(
        @Query("serviceKey", encoded = true) serviceKey : String = INDOOR_EVACUATION_SERVICE_KEY,
        @Query("pageNo") pageNo : String = "2",             // 페이지번호
        @Query("numOfRows") numOfRows : String = "20",      // 한페이지 결과수
        @Query("type") type : String = "JSON", // 호출문서형식
    ) : EarthquakeIndoorsShelterResponse
}