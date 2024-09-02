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

    override suspend fun requestOutdoorEvacuation(
        arcd: String,
        ctprvnNm: String,
        sggNm: String
    ): EarthquakeOutdoorsShelterResponse {
        return outdoorEvacuationService.getOutdoorEvacuation(
            arcd = arcd,
            ctprvnNm = ctprvnNm,
            sggNm = sggNm
        )
    }
}
