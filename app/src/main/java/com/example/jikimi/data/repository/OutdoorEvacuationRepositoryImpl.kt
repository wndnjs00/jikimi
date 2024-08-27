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
        pageNo: String,
        numOfRows: String,
        type: String
    ): EarthquakeOutdoorsShelterResponse {
        return outdoorEvacuationService.getOutdoorEvacuation(
            pageNo = "1",
            numOfRows = "10",
            type = "JSON",
            arcd = "4380000000",
            ctprvnNm = "충청북도",
            sggNm = "단양군",
        )
    }
}