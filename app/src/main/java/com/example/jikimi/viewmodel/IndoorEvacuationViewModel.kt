package com.example.jikimi.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jikimi.data.model.dto.EarthquakeIndoorsShelterResponse
import com.example.jikimi.data.network.distanceExtention
import com.example.jikimi.data.repository.IndoorEvacuationRepository
import com.naver.maps.geometry.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IndoorEvacuationViewModel @Inject constructor(
    private val indoorEvacuationRepository: IndoorEvacuationRepository
) : ViewModel(){

    private val _shelter = MutableStateFlow<List<EarthquakeIndoorsShelterResponse.EarthquakeIndoor.Row>>(emptyList())
    val shelter : StateFlow<List<EarthquakeIndoorsShelterResponse.EarthquakeIndoor.Row>> = _shelter

    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentLocation: StateFlow<LatLng?> = _currentLocation

    // 로딩 상태
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // 에러 상태
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage


    // API데이터를 가져오고 업데이트 (중복 호출 방지 및 개선된 필터링 로직)
    fun fetchIndoorShelters(currentAddress: String) {
        // 이미 로딩 중이면 중복 호출 방지
        if (_isLoading.value) {
            Log.d("IndoorEvacuationViewModel", "Already loading data, skipping request")
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                // 모든 페이지의 데이터 요청
                val allShelters = indoorEvacuationRepository.requestAllIndoorEvacuation()
                Log.d("IndoorEvacuationViewModel", "Total shelters retrieved: ${allShelters.size}")

                // currentLocation이 있는 경우만 거리 기반 필터링 수행
                _currentLocation.value?.let { location ->
                    // 반경 5km 이내의 대피소만 필터링
                    val filteredShelters = allShelters.filter { shelter ->
                        val latitude = shelter.ycord.toDoubleOrNull() ?: 0.0
                        val longitude = shelter.xcord.toDoubleOrNull() ?: 0.0

                        if (latitude != 0.0 && longitude != 0.0) {
                            val shelterLocation = LatLng(latitude, longitude)
                            val distance = location.distanceExtention(shelterLocation)
                            distance <= 5000.0 // 5km
                        } else {
                            false
                        }
                    }.sortedBy { shelter ->
                        // 가까운 대피소부터 정렬
                        val latitude = shelter.ycord.toDoubleOrNull() ?: 0.0
                        val longitude = shelter.xcord.toDoubleOrNull() ?: 0.0
                        val shelterLocation = LatLng(latitude, longitude)
                        location.distanceExtention(shelterLocation)
                    }

                    // 필터링한 대피소데이터를 shelters에 업데이트
                    _shelter.value = filteredShelters
                    Log.d("IndoorEvacuationViewModel", "Shelters found within 5km: ${filteredShelters.size}")
                } ?: run {
                    // 주소 기반 필터링 - 개선된 버전
                    val adminKeywords = currentAddress.split(" ").filter { it.length >= 2 }
                    val filteredByAddress = if (adminKeywords.isNotEmpty()) {
                        allShelters.filter { shelter ->
                            val shelterAddress = shelter.rnAdres ?: ""
                            adminKeywords.any { keyword ->
                                shelterAddress.contains(keyword)
                            }
                        }
                    } else {
                        // 주소가 없으면 모든 대피소 반환
                        allShelters
                    }
                    _shelter.value = filteredByAddress
                    Log.d("IndoorEvacuationViewModel", "Shelters filtered by address: ${filteredByAddress.size}")
                }

            } catch (e: Exception) {
                Log.e("IndoorEvacuationViewModel", "API 받아오기 실패: ${e.message}", e)
                _errorMessage.value = "Indoor대피소 정보를 불러오는데 실패했습니다: ${e.message}"
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

