package com.example.jikimi.data.repository

import android.content.Context
import android.util.Log
import com.example.jikimi.data.local.dao.ShelterDao
import com.example.jikimi.data.model.dto.EarthquakeIndoorsShelterResponse
import com.example.jikimi.data.model.entity.ShelterEntity
import com.example.jikimi.data.network.KEY_LAST_INDOOR_UPDATE
import com.example.jikimi.data.network.KEY_TOTAL_INDOOR_COUNT
import com.example.jikimi.data.network.PREF_NAME
import com.example.jikimi.data.network.UPDATE_INTERVAL
import com.example.jikimi.data.network.service.IndoorEvacuationService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class IndoorEvacuationRepositoryImpl @Inject constructor(
    @Named("IndoorEvacuationService") private val indoorEvacuationService: IndoorEvacuationService,
    private val shelterDao: ShelterDao,
    @ApplicationContext private val context: Context
) : IndoorEvacuationRepository{

    private val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    override suspend fun requestIndoorEvacuation(): EarthquakeIndoorsShelterResponse {
        return indoorEvacuationService.getIndoorEvacuation(pageNo = "1")
    }

    // 특정 페이지 요청 구현
    override suspend fun requestIndoorEvacuationByPage(pageNo: Int): EarthquakeIndoorsShelterResponse {
        return indoorEvacuationService.getIndoorEvacuation(pageNo = pageNo.toString())
    }

    // 모든 데이터 요청 구현
    override suspend fun requestAllIndoorEvacuation(): List<EarthquakeIndoorsShelterResponse.EarthquakeIndoor.Row> {
        try {
            // 캐시된 데이터가 있고, 갱신 주기가 지나지 않았다면 DB에서 데이터 반환
            val lastUpdate = sharedPreferences.getLong(KEY_LAST_INDOOR_UPDATE, 0)
            val totalCachedCount = sharedPreferences.getInt(KEY_TOTAL_INDOOR_COUNT, 0)
            val dbCount = shelterDao.getShelterCountByType("임시주거시설")

            val currentTime = System.currentTimeMillis()
            val needsUpdate = currentTime - lastUpdate > UPDATE_INTERVAL || dbCount < totalCachedCount * 0.9

            if (!needsUpdate && dbCount > 0) {
                Log.d("IndoorEvacuationRepo", "실내 대피소 정보를 캐시에서 로드합니다. DB 항목 수: $dbCount")
                // DB에서 데이터를 가져와 API 응답 형식으로 변환
                return convertDbToApiFormat(shelterDao.getSheltersByType("임시주거시설").first())
            }

            // API에서 첫 페이지 요청하여 총 데이터 개수 확인
            Log.d("IndoorEvacuationRepo", "실내 대피소 API 호출을 시작합니다")
            val firstPageResponse = requestIndoorEvacuationByPage(1)
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

            // 첫 페이지 데이터 추가
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
                    continue
                }
            }

            // 데이터를 DB에 저장
            saveIndoorSheltersToDB(allRows)

            // 마지막 업데이트 시간과 총 항목 수 저장
            sharedPreferences.edit()
                .putLong(KEY_LAST_INDOOR_UPDATE, System.currentTimeMillis())
                .putInt(KEY_TOTAL_INDOOR_COUNT, totalCount)
                .apply()

            Log.d("IndoorEvacuationRepo", "총 ${allRows.size}개의 실내 대피소 데이터를 저장했습니다")
            return allRows
        } catch (e: Exception) {
            Log.e("IndoorEvacuationRepo", "전체 데이터 요청 실패: ${e.message}", e)

            // API 호출 실패 시 캐시된 데이터 반환
            val cachedData = shelterDao.getSheltersByType("임시주거시설").first()
            Log.d("IndoorEvacuationRepo", "캐시된 데이터 사용: ${cachedData.size}개 항목")

            return if (cachedData.isNotEmpty()) {
                convertDbToApiFormat(cachedData)
            } else {
                emptyList()
            }
        }
    }


    // DB의 엔티티를 API 응답 형식으로 변환
    private fun convertDbToApiFormat(entities: List<ShelterEntity>): List<EarthquakeIndoorsShelterResponse.EarthquakeIndoor.Row> {
        return entities.map { entity ->
            EarthquakeIndoorsShelterResponse.EarthquakeIndoor.Row(
                vtAcmdfcltyNm = entity.vtAcmdfcltyNm,
                xcord = entity.longitude.toString(),
                ycord = entity.latitude.toString(),
                rnAdres = entity.address ?: "",
                dtlAdres = entity.detailAddress ?: "",
                mngpsTelno = entity.mngpsTelno,
                acmdfcltyDtlCn = entity.acmdfcltyDtlCn,
                vtAcmdPsblNmpr = entity.vtAcmdPsblNmpr,
                arcd = "",
                acmdfcltySn = "",
                ctprvnNm = "",
                sggNm = "",
                rdnmadrCd = "",
                bdongCd = "",
                fcltyAr = "",
                hdongCd = "",
                mngpsNm = "",
                mngdptNm = "",
            )
        }
    }

    // 실내 대피소 데이터 저장 (기존 데이터 삭제 후 새로 저장)
    private suspend fun saveIndoorSheltersToDB(shelters: List<EarthquakeIndoorsShelterResponse.EarthquakeIndoor.Row>) {
        withContext(Dispatchers.IO) {
            // 기존 실내 대피소 데이터 삭제
            shelterDao.deleteSheltersByType("임시주거시설")

            // 새 데이터를 엔티티로 변환하여 저장
            val entities = shelters.mapNotNull { shelter ->
                val latitude = shelter.ycord.toDoubleOrNull() ?: return@mapNotNull null
                val longitude = shelter.xcord.toDoubleOrNull() ?: return@mapNotNull null

                ShelterEntity(
                    vtAcmdfcltyNm = shelter.vtAcmdfcltyNm ?: "이름 없음",
                    address = shelter.rnAdres ?: "주소 없음",
                    detailAddress = shelter.dtlAdres ?: "",
                    latitude = latitude,
                    longitude = longitude,
                    shelterType = "임시주거시설",
                    vtAcmdPsblNmpr = shelter.vtAcmdPsblNmpr ?: "알수없음",
                    acmdfcltyDtlCn = shelter.acmdfcltyDtlCn ?: "알수없음",
                    mngpsTelno = shelter.mngpsTelno ?: "정보없음",
                    lastUpdated = System.currentTimeMillis()
                )
            }

            if (entities.isNotEmpty()) {
                shelterDao.insertShelters(entities)
            }
        }
    }
}