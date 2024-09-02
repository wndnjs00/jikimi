package com.example.jikimi.data.repository

import com.example.jikimi.data.model.dto.EarthquakeOutdoorsShelterResponse

interface OutdoorEvacuationRepository {
    suspend fun requestOutdoorEvacuation(arcd :String, ctprvnNm : String, sggNm : String) : EarthquakeOutdoorsShelterResponse
}