package com.example.jikimi.data.repository

import com.example.jikimi.data.model.dto.EarthquakeIndoorsShelterResponse

interface IndoorEvacuationRepository {
    suspend fun requestIndoorEvacuation() : EarthquakeIndoorsShelterResponse
}