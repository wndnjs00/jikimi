package com.example.jikimi.data.remote

import com.example.jikimi.data.network.OutdoorEvacuationService
import javax.inject.Inject
import javax.inject.Named

class EvacuationDataSource @Inject constructor(
    @Named("OutdoorEvacuationService") private val outdoorEvacuationService: OutdoorEvacuationService
) {
    suspend fun getOutdoorEvacuation(
        pageNo : String = "1",
        numOfRows : String = "5",
        arcd : String = "4380000000",
        ctprvnNm : String = "충청북도",
        sggNm : String = "단양군"
    ) = outdoorEvacuationService.getOutdoorEvacuation(
        pageNo = pageNo,
        numOfRows = numOfRows,
        arcd = arcd,
        ctprvnNm = ctprvnNm,
        sggNm = sggNm
    )
}