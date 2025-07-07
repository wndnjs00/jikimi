package com.myapp.jikimi.data.repository.OutdoorEvacuation

import com.myapp.jikimi.data.model.dto.EarthquakeOutdoorsShelterResponse

interface OutdoorEvacuationRepository {

    // 특정 페이지 요청
    suspend fun requestOutdoorEvacuationByPage(pageNo: Int): EarthquakeOutdoorsShelterResponse

    // 모든 데이터 요청
    suspend fun requestAllOutdoorEvacuation(): List<EarthquakeOutdoorsShelterResponse.Shelter>
}