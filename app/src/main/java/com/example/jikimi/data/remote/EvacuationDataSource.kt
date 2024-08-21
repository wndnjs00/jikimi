package com.example.jikimi.data.remote

import com.example.jikimi.data.network.OutdoorEvacuationService
import javax.inject.Inject
import javax.inject.Named

class EvacuationDataSource @Inject constructor(
    @Named("OutdoorEvacuationService") private val outdoorEvacuationService: OutdoorEvacuationService
) {
    suspend fun getOutdoorEvacuation(
        pageNo : String = "1",
        numOfRows : String = "10",
        arcd : String = "",
        ctprvnNm : String = "",
        sggNm : String = ""
    ) = outdoorEvacuationService.getOutdoorEvacuation(
        pageNo = pageNo,
        numOfRows = numOfRows,
        arcd = arcd,
        ctprvnNm = ctprvnNm,
        sggNm = sggNm
    )
}