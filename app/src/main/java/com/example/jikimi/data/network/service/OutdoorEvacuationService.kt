package com.example.jikimi.data.network.service

import com.example.jikimi.data.model.dto.EarthquakeOutdoorsShelterResponse
import com.example.jikimi.data.network.OUTDOOR_EVACUATION_API
import com.example.jikimi.data.network.OUTDOOR_EVACUATION_SERVICE_KEY
import retrofit2.http.GET
import retrofit2.http.Query

// 행정안전부_지진 옥외대피장소 API
interface OutdoorEvacuationService {
    @GET(OUTDOOR_EVACUATION_API)

    suspend fun getOutdoorEvacuation(
        @Query("serviceKey") serviceKey : String = OUTDOOR_EVACUATION_SERVICE_KEY,
        @Query("pageNo") pageNo : String = "1",             // 페이지번호
        @Query("numOfRows") numOfRows : String = "50",      // 한페이지 결과수 (나중에 200으로 바꾸기)
        @Query("type") type : String = "JSON", // 호출문서형식
        @Query("ctprvn_nm") ctprvnNm : String, // 시도명
    ) : EarthquakeOutdoorsShelterResponse
}