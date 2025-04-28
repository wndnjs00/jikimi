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

    // 모든 데이터 요청 구현 - 수정된 부분
    override suspend fun requestAllIndoorEvacuation(): List<EarthquakeIndoorsShelterResponse.EarthquakeIndoor.Row> {
        try {
            // 첫 페이지 요청하여 총 데이터 개수 확인
            val firstPageResponse = requestIndoorEvacuationByPage(1)

            // API 응답 구조에 맞게 수정
            val totalCount = firstPageResponse.earthquakeIndoors[0].head[0].totalCount?.toIntOrNull() ?: 0
            Log.d("IndoorEvacuationRepo", "총 대피소 개수: $totalCount")

            val itemsPerPage = 100

            // 총 페이지 수 계산
            val totalPages = if (totalCount % itemsPerPage == 0) {
                totalCount / itemsPerPage
            } else {
                (totalCount / itemsPerPage) + 1
            }

            // 모든 대피소 데이터를 담을 리스트
            val allRows = mutableListOf<EarthquakeIndoorsShelterResponse.EarthquakeIndoor.Row>()

            // 첫 페이지 데이터 추가 - row 객체는 두 번째 항목에 있음
            if (firstPageResponse.earthquakeIndoors.size > 1) {
                firstPageResponse.earthquakeIndoors[1].row?.let { rows ->
                    allRows.addAll(rows)
                }
            }

            // 나머지 페이지 데이터 요청
            for (page in 2..totalPages) {
                try {
                    val response = requestIndoorEvacuationByPage(page)
                    if (response.earthquakeIndoors.size > 1) {
                        response.earthquakeIndoors[1].row?.let { rows ->
                            allRows.addAll(rows)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("IndoorEvacuationRepo", "페이지 $page 데이터 요청 실패: ${e.message}", e)
                    // 에러 발생해도 이미 받은 데이터는 사용
                    continue
                }
            }

            Log.d("IndoorEvacuationRepo", "총 ${allRows.size}개의 실내 대피소 데이터를 가져왔습니다.")
            return allRows
        } catch (e: Exception) {
            Log.e("IndoorEvacuationRepo", "전체 데이터 요청 중 오류 발생: ${e.message}", e)
            return emptyList()
        }
    }
}