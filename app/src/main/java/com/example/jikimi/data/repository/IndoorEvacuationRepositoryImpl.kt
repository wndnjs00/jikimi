package com.example.jikimi.data.repository

import com.example.jikimi.data.model.dto.EarthquakeIndoorsShelterResponse
import com.example.jikimi.data.network.IndoorEvacuationService
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class IndoorEvacuationRepositoryImpl @Inject constructor(
    @Named("IndoorEvacuationService") private val indoorEvacuationService: IndoorEvacuationService
) : IndoorEvacuationRepository{

    override suspend fun requestIndoorEvacuation(): EarthquakeIndoorsShelterResponse {
        return indoorEvacuationService.getIndoorEvacuation()
    }
}