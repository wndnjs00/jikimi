package com.example.jikimi.data.repository

import com.example.jikimi.data.model.dto.EarthquakeOutdoorsShelterResponse

interface OutdoorEvacuationRepository {
    suspend fun requestOutdoorEvacuation() : EarthquakeOutdoorsShelterResponse
}