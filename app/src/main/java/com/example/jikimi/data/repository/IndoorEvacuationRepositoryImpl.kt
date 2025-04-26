package com.example.jikimi.data.repository

import android.util.Log
import com.example.jikimi.data.model.dto.EarthquakeIndoorsShelterResponse
import com.example.jikimi.data.network.service.IndoorEvacuationService
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class IndoorEvacuationRepositoryImpl @Inject constructor(
    @Named("IndoorEvacuationService") private val indoorEvacuationService: IndoorEvacuationService
) : IndoorEvacuationRepository{

    override suspend fun requestIndoorEvacuation(): EarthquakeIndoorsShelterResponse {
        return indoorEvacuationService.getIndoorEvacuation(pageNo = "1")
    }

    // 특정 페이지 요청 구현
    override suspend fun requestIndoorEvacuationByPage(pageNo: Int): EarthquakeIndoorsShelterResponse {
        return indoorEvacuationService.getIndoorEvacuation(pageNo = pageNo.toString())
    }

    // 모든 데이터 요청 구현
    override suspend fun requestAllIndoorEvacuation(): List<EarthquakeIndoorsShelterResponse.EarthquakeIndoor.Row> {
        // 첫 페이지 요청하여 총 데이터 개수 확인
        val firstPageResponse = requestIndoorEvacuationByPage(1)
        val totalCount = firstPageResponse.earthquakeIndoors?.getOrNull(0)?.head?.getOrNull(0)?.totalCount?.toIntOrNull() ?: 0
        val itemsPerPage = 100

        // 총 페이지 수 계산
        val totalPages = if (totalCount % itemsPerPage == 0) {
            totalCount / itemsPerPage
        } else {
            (totalCount / itemsPerPage) + 1
        }

        // 모든 대피소 데이터를 담을 리스트
        val allRows = mutableListOf<EarthquakeIndoorsShelterResponse.EarthquakeIndoor.Row>()

        // 첫 페이지 데이터 추가
        firstPageResponse.earthquakeIndoors?.getOrNull(0)?.row?.let { rows ->
            allRows.addAll(rows)
        }

        // 나머지 페이지 데이터 요청
        for (page in 2..totalPages) {
            try {
                val response = requestIndoorEvacuationByPage(page)
                response.earthquakeIndoors?.getOrNull(0)?.row?.let { rows ->
                    allRows.addAll(rows)
                }
            } catch (e: Exception) {
                Log.e("IndoorEvacuationRepo", "페이지 $page 데이터 요청 실패: ${e.message}")
                // 에러 발생해도 이미 받은 데이터는 사용
                continue
            }
        }
        return allRows
    }
}