package com.example.jikimi.data.repository

import com.example.jikimi.data.model.dto.EarthquakeOutdoorsShelterResponse
import com.example.jikimi.data.network.OutdoorEvacuationService
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class OutdoorEvacuationRepositoryImpl @Inject constructor(
    @Named("OutdoorEvacuationService") private val outdoorEvacuationService : OutdoorEvacuationService
) : OutdoorEvacuationRepository{

    override suspend fun requestOutdoorEvacuation(ctprvnNm: String): EarthquakeOutdoorsShelterResponse {
        return outdoorEvacuationService.getOutdoorEvacuation(ctprvnNm = ctprvnNm)
    }
}
