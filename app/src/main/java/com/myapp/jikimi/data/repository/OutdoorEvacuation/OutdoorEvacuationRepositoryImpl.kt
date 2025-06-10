package com.myapp.jikimi.data.repository.OutdoorEvacuation

import android.content.Context
import android.util.Log
import com.myapp.jikimi.data.local.dao.ShelterDao
import com.myapp.jikimi.data.model.dto.EarthquakeOutdoorsShelterResponse
import com.myapp.jikimi.data.model.entity.ShelterEntity
import com.myapp.jikimi.data.network.KEY_LAST_OUTDOOR_UPDATE
import com.myapp.jikimi.data.network.KEY_TOTAL_OUTDOOR_COUNT
import com.myapp.jikimi.data.network.PREF_NAME
import com.myapp.jikimi.data.network.UPDATE_INTERVAL
import com.myapp.jikimi.data.network.service.OutdoorEvacuationService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class OutdoorEvacuationRepositoryImpl @Inject constructor(
    @Named("OutdoorEvacuationService") private val outdoorEvacuationService: OutdoorEvacuationService,
    private val shelterDao: ShelterDao,
    @ApplicationContext private val context: Context
) : OutdoorEvacuationRepository {

    private val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    override suspend fun requestOutdoorEvacuation(): EarthquakeOutdoorsShelterResponse {
        return outdoorEvacuationService.getOutdoorEvacuation(pageNo = "1")
    }

    // 특정 페이지 요청 구현
    override suspend fun requestOutdoorEvacuationByPage(pageNo: Int): EarthquakeOutdoorsShelterResponse {
        return outdoorEvacuationService.getOutdoorEvacuation(pageNo = pageNo.toString())
    }

    // 모든 데이터 요청 구현
    override suspend fun requestAllOutdoorEvacuation(): List<EarthquakeOutdoorsShelterResponse.Shelter> {
        try {
            // 데이터를 다시 로드할 필요가 있는지 판단 (sharedPreferences로 캐시에 저장된 데이터가 최신인지 확인하여, 불필요한 API 호출을 방지)
            val lastUpdate = sharedPreferences.getLong(KEY_LAST_OUTDOOR_UPDATE, 0)  //마지막 업데이트 시간
            val totalCachedCount = sharedPreferences.getInt(KEY_TOTAL_OUTDOOR_COUNT, 0) //전체 대피소의 개수
            val dbCount = shelterDao.getShelterCountByType("야외대피장소")    //"야외대피장소"의 대피소수

            val currentTime = System.currentTimeMillis()
            // 마지막 업데이트 이후 30일이 지났는지 or DB에 저장된 데이터수가 API에서 가져온 전체데이터의 90% 이상인지
            val needsUpdate = currentTime - lastUpdate > UPDATE_INTERVAL || dbCount < totalCachedCount * 0.9

            // 만족하지않으면(갱신필요하지 않으면)
            if (!needsUpdate && dbCount > 0) {
                Log.d("OutdoorEvacuationRepo", "야외 대피소 정보를 캐시에서 로드합니다. DB 항목 수: $dbCount")
                // RoomDB에서 데이터를 반환
                return convertDbToApiFormat(shelterDao.getSheltersByType("야외대피장소").first())
            }

            // API에서 첫 페이지 요청하여 전체데이터 개수 확인
            Log.d("OutdoorEvacuationRepo", "야외 대피소 API 호출을 시작합니다")
            val firstPageResponse = requestOutdoorEvacuationByPage(1)
            val totalCount = firstPageResponse.totalCount
            Log.d("OutdoorEvacuationRepo", "총 대피소 개수: $totalCount")
            val itemsPerPage = 100

            // 총 페이지 수 계산
            val totalPages = if (totalCount % itemsPerPage == 0) {
                totalCount / itemsPerPage
            } else {
                (totalCount / itemsPerPage) + 1
            }

            // 첫 페이지 데이터 추가
            val allShelters = firstPageResponse.body.toMutableList()

            // 나머지 페이지 데이터 요청
            for (page in 2..totalPages) {
                try {
                    val response = requestOutdoorEvacuationByPage(page)
                    allShelters.addAll(response.body)
                } catch (e: Exception) {
                    Log.e("OutdoorEvacuationRepo", "페이지 $page 데이터 요청 실패: ${e.message}")
                    continue
                }
            }

            // 데이터를 RoomDB에 저장
            saveOutdoorSheltersToDB(allShelters)

            // 마지막 업데이트시간과 총항목수 sharedPreferences로 저장
            sharedPreferences.edit()
                .putLong(KEY_LAST_OUTDOOR_UPDATE, System.currentTimeMillis())
                .putInt(KEY_TOTAL_OUTDOOR_COUNT, totalCount)
                .apply()
            Log.d("OutdoorEvacuationRepo", "총 ${allShelters.size}개의 야외 대피소 데이터를 저장했습니다")
            return allShelters
        } catch (e: Exception) {
            Log.e("OutdoorEvacuationRepo", "전체 데이터 요청 실패: ${e.message}", e)

            // API 호출 실패 시 캐시된 데이터 반환
            val cachedData = shelterDao.getSheltersByType("야외대피장소").first()
            Log.d("OutdoorEvacuationRepo", "캐시된 데이터 사용: ${cachedData.size}개 항목")

            return if (cachedData.isNotEmpty()) {
                convertDbToApiFormat(cachedData)
            } else {
                emptyList()
            }
        }
    }


    // DB의 엔티티를 API 응답 형식으로 변환
    private fun convertDbToApiFormat(entities: List<ShelterEntity>): List<EarthquakeOutdoorsShelterResponse.Shelter> {
        return entities.map { entity ->
            EarthquakeOutdoorsShelterResponse.Shelter(
                vtAcmdfcltyNm = entity.vtAcmdfcltyNm,
                la = entity.latitude.toString(),
                lo = entity.longitude.toString(),
                eqkAcmdfcltyAdres = entity.address,
                dtlAdres = entity.detailAddress,
                vtAcmdPsblNmpr = entity.vtAcmdPsblNmpr,
                rnDtlAdres = entity.detailAddress,
                useSeCd = "",
                acmdBuldMngNo = "",
                bdongCd = "",
                arcd = "",
                hdongCd = "",
                acmdfcltySn = 0,
            )
        }
    }

    // 야외 대피소 데이터 RoomDB에 저장 (기존 데이터 삭제 후 새로 저장)
    private suspend fun saveOutdoorSheltersToDB(shelters: List<EarthquakeOutdoorsShelterResponse.Shelter>) {
        withContext(Dispatchers.IO) {
            // 기존 야외대피소 데이터 삭제
            shelterDao.deleteSheltersByType("야외대피장소")

            // 새 데이터를 ShelterEntity형식으로 변환하여 저장
            val entities = shelters.mapNotNull { shelter ->
                val latitude = shelter.la?.toDoubleOrNull() ?: return@mapNotNull null
                val longitude = shelter.lo?.toDoubleOrNull() ?: return@mapNotNull null

                ShelterEntity(
                    vtAcmdfcltyNm = shelter.vtAcmdfcltyNm ?: "이름 없음",
                    address = shelter.eqkAcmdfcltyAdres ?: "주소 없음",
                    detailAddress = shelter.dtlAdres ?: "",
                    latitude = latitude,
                    longitude = longitude,
                    shelterType = "야외대피장소",
                    vtAcmdPsblNmpr = shelter.vtAcmdPsblNmpr ?: "알수없음",
                    lastUpdated = System.currentTimeMillis(),
                    acmdfcltyDtlCn = "",
                    mngpsTelno = "",
                )
            }

            if (entities.isNotEmpty()) {
                shelterDao.insertShelters(entities)
            }
        }
    }
}