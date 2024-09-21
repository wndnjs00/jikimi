package com.example.jikimi.data.network

import com.example.jikimi.data.model.dto.EarthquakeOutdoorsShelterResponse
import retrofit2.http.GET
import retrofit2.http.Query

// 필수로 들어가야하는 쿼리작성
// 행정안전부_지진 옥외대피장소 API
interface OutdoorEvacuationService {
    @GET(OUTDOOR_EVACUATION_API)

    suspend fun getOutdoorEvacuation(
        @Query("serviceKey") serviceKey : String = OUTDOOR_EVACUATION_SERVICE_KEY,
        @Query("pageNo") pageNo : String = "1",             // 페이지번호
        @Query("numOfRows") numOfRows : String = "1",      // 한페이지 결과수
        @Query("type") type : String = "JSON", // 호출문서형식
        @Query("arcd") arcd : String, // 지역코드
        @Query("ctprvn_nm") ctprvnNm : String, // 시도명
        @Query("sgg_nm") sggNm : String, // 시군구명
    ) : EarthquakeOutdoorsShelterResponse

}