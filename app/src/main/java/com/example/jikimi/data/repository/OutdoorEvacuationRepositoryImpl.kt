package com.example.jikimi.data.repository

import android.util.Log
import com.example.jikimi.data.model.dto.EarthquakeOutdoorsShelterResponse
import com.example.jikimi.data.network.service.OutdoorEvacuationService
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class OutdoorEvacuationRepositoryImpl @Inject constructor(
    @Named("OutdoorEvacuationService") private val outdoorEvacuationService : OutdoorEvacuationService
) : OutdoorEvacuationRepository{

    override suspend fun requestOutdoorEvacuation(): EarthquakeOutdoorsShelterResponse {
        return outdoorEvacuationService.getOutdoorEvacuation(pageNo = "1")
    }

    // 특정 페이지 요청 구현
    override suspend fun requestOutdoorEvacuationByPage(pageNo: Int): EarthquakeOutdoorsShelterResponse {
        return outdoorEvacuationService.getOutdoorEvacuation(pageNo = pageNo.toString())
    }

    // 모든 데이터 요청 구현
    override suspend fun requestAllOutdoorEvacuation(): List<EarthquakeOutdoorsShelterResponse.Shelter> {
        // 첫 페이지 요청하여 총 데이터 개수 확인
        val firstPageResponse = requestOutdoorEvacuationByPage(1)
        val totalCount = firstPageResponse.totalCount
        val itemsPerPage = 100

        // 총 페이지 수 계산
        val totalPages = if (totalCount % itemsPerPage == 0) {
            totalCount / itemsPerPage
        } else {
            (totalCount / itemsPerPage) + 1
        }

        // 첫 페이지 데이터 저장
        val allShelters = firstPageResponse.body.toMutableList()

        // 나머지 페이지 데이터 요청
        for (page in 2..totalPages) {
            try {
                val response = requestOutdoorEvacuationByPage(page)
                allShelters.addAll(response.body)
            } catch (e: Exception) {
                Log.e("OutdoorEvacuationRepo", "페이지 $page 데이터 요청 실패: ${e.message}")
                // 에러 발생해도 이미 받은 데이터는 사용
                continue
            }
        }

        return allShelters
    }
}
