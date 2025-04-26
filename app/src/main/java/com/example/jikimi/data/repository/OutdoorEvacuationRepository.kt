package com.example.jikimi.data.repository

import com.example.jikimi.data.model.dto.EarthquakeOutdoorsShelterResponse

interface OutdoorEvacuationRepository {
    suspend fun requestOutdoorEvacuation() : EarthquakeOutdoorsShelterResponse

    // 특정 페이지 요청
    suspend fun requestOutdoorEvacuationByPage(pageNo: Int): EarthquakeOutdoorsShelterResponse

    // 모든 데이터 요청
    suspend fun requestAllOutdoorEvacuation(): List<EarthquakeOutdoorsShelterResponse.Shelter>
}