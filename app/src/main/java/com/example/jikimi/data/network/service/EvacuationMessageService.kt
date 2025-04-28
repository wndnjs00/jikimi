package com.example.jikimi.data.network.service

import com.example.jikimi.data.model.dto.EvacuationMessageResponse
import com.example.jikimi.data.network.EVACUATION_MESSAGE_API
import com.example.jikimi.data.network.EVACUATION_MESSAGE_SERVICE_KEY
import com.example.jikimi.data.network.INDOOR_EVACUATION_API
import retrofit2.http.GET
import retrofit2.http.Query

// 재난 안내문자 API
interface EvacuationMessageService {
    @GET(EVACUATION_MESSAGE_API)

    suspend fun getEvacuationMessage(
        @Query("serviceKey", encoded = true) serviceKey : String = EVACUATION_MESSAGE_SERVICE_KEY,
        @Query("pageNo") pageNo : String = "1",             // 페이지번호
        @Query("numOfRows") numOfRows : String = "1",       // 한페이지 결과수
        @Query("crtDt") crtDt: String,                      // 조회시작일자(YYYYMMDD)
        @Query("returnType") returnType : String = "JSON",  // 호출문서형식
    ) : EvacuationMessageResponse
}