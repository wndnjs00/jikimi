package com.myapp.jikimi.data.repository.IndoorEvacuation

import com.myapp.jikimi.data.model.dto.EarthquakeIndoorsShelterResponse

interface IndoorEvacuationRepository {
    suspend fun requestIndoorEvacuation() : EarthquakeIndoorsShelterResponse
    // 특정 페이지 요청
    suspend fun requestIndoorEvacuationByPage(pageNo: Int): EarthquakeIndoorsShelterResponse
    // 모든 데이터 요청
    suspend fun requestAllIndoorEvacuation(): List<EarthquakeIndoorsShelterResponse.EarthquakeIndoor.Row>
}