package com.myapp.jikimi.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapp.jikimi.data.local.dao.ShelterDao
import com.myapp.jikimi.data.model.dto.EarthquakeOutdoorsShelterResponse
import com.myapp.jikimi.data.network.haversineDistance
import com.myapp.jikimi.data.repository.OutdoorEvacuationRepository
import com.naver.maps.geometry.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OutdoorEvacuationViewModel @Inject constructor(
    private val outdoorEvacuationRepository: OutdoorEvacuationRepository,
) : ViewModel() {

    private val _shelters = MutableStateFlow<List<EarthquakeOutdoorsShelterResponse.Shelter>>(emptyList())
    val shelters: StateFlow<List<EarthquakeOutdoorsShelterResponse.Shelter>> = _shelters

    // 위치 데이터
    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentLocation: StateFlow<LatLng?> = _currentLocation

    // 로딩 상태
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // 에러 상태
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // API로 currentAddress 데이터를 가져오고 업데이트 (API 호출 최적화)
    fun fetchOutdoorShelters(currentAddress: String) {
        // 이미 로딩 중이면 중복 호출 방지
        if (_isLoading.value) {
            Log.d("OutdoorEvacuationViewModel", "이미 로딩 중입니다")
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null  //이전에 남아있을수있는 에러메시지 초기화

                // 모든 페이지의 데이터 요청(로컬 DB 캐시 활용)
                val allShelters = outdoorEvacuationRepository.requestAllOutdoorEvacuation()
                Log.d("OutdoorEvacuationViewModel", "가져온 대피소 수: ${allShelters.size}")

                // 현재 위치를 사용할 수 있는 경우 거리별 필터링
                _currentLocation.value?.let { location ->
                    // 반경 5km 이내의 대피소만 필터링
                    val filteredShelters = allShelters.filter { shelter ->
                        val latitude = shelter.la?.toDoubleOrNull() ?: 0.0
                        val longitude = shelter.lo?.toDoubleOrNull() ?: 0.0

                        if (latitude != 0.0 && longitude != 0.0) {
                            val shelterLocation = LatLng(latitude, longitude)
                            val distance = location.haversineDistance(shelterLocation)  //현재위치와 대피소간의 거리계산
                            distance <= 5000.0 // 거리가 5km이하인 대피소만 필터링
                        } else {
                            false
                        }
                    }.sortedBy { shelter ->
                        // 가까운 대피소부터 정렬
                        val latitude = shelter.la?.toDoubleOrNull() ?: 0.0
                        val longitude = shelter.lo?.toDoubleOrNull() ?: 0.0
                        val shelterLocation = LatLng(latitude, longitude)
                        location.haversineDistance(shelterLocation)
                    }

                    // 필터링된 데이터로 대피소 업데이트
                    _shelters.value = filteredShelters
                    Log.d("OutdoorEvacuationViewModel", "5km 내 대피소 수: ${filteredShelters.size}")

                } ?: run {
                    // 주소 기반 필터링
                    val adminKeywords = currentAddress.split(" ").filter { it.length >= 2 }
                    val filteredByAddress = if (adminKeywords.isNotEmpty()) {
                        allShelters.filter { shelter ->
                            val shelterAddress = shelter.eqkAcmdfcltyAdres ?: ""
                            adminKeywords.any { keyword ->
                                shelterAddress.contains(keyword)
                            }
                        }
                    } else {
                        // 주소가 없으면 모든 대피소 반환
                        allShelters
                    }
                    _shelters.value = filteredByAddress
                    Log.d("OutdoorEvacuationViewModel", "주소 기준 필터링 결과: ${filteredByAddress.size}")
                }

            } catch (e: Exception) {
                Log.e("OutdoorEvacuationViewModel", "API 요청 실패: ${e.message}", e)
                _errorMessage.value = "outdoor대피소 정보를 불러오는데 실패했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }


    // 위치 업데이트 메서드
    fun updateCurrentLocation(latitude: Double, longitude: Double) {
        _currentLocation.value = LatLng(latitude, longitude)
    }

    // 에러 메시지 초기화
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}