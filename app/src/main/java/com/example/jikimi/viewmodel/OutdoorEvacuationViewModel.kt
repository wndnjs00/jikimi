package com.example.jikimi.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jikimi.data.model.dto.EarthquakeOutdoorsShelterResponse
import com.example.jikimi.data.network.distanceExtention
import com.example.jikimi.data.repository.OutdoorEvacuationRepository
import com.naver.maps.geometry.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OutdoorEvacuationViewModel @Inject constructor(
    private val outdoorEvacuationRepository: OutdoorEvacuationRepository
) : ViewModel() {

    private val _shelters = MutableStateFlow<List<EarthquakeOutdoorsShelterResponse.Shelter>>(emptyList())
    val shelters: StateFlow<List<EarthquakeOutdoorsShelterResponse.Shelter>> = _shelters

    // 위치 데이터
    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentLocation: StateFlow<LatLng?> = _currentLocation

    // API 요청 중인지 확인하는 플래그 추가
    private var isLoading = false


    // API로 currentAddress 데이터를 가져오고 업데이트
    fun fetchOutdoorShelters(currentAddress: String) {
        // 이미 로딩 중이면 중복 호출 방지
        if (isLoading) {
            Log.d("OutdoorEvacuationViewModel", "Already loading data, skipping request")
            return
        }

        viewModelScope.launch {
            try {
                isLoading = true
                // API 호출
                val response = outdoorEvacuationRepository.requestOutdoorEvacuation()
                Log.d("OutdoorEvacuationViewModel_response", "Response received: $response")

                val sheltersList = response.body ?: emptyList()

                // 현재 위치를 사용할 수 있는 경우 거리별 필터링
                _currentLocation.value?.let { location ->
                    // 반경 5km 이내의 대피소만 필터링
                    val filteredShelters = sheltersList.filter { shelter ->
                        val latitude = shelter.la?.toDoubleOrNull() ?: 0.0
                        val longitude = shelter.lo?.toDoubleOrNull() ?: 0.0

                        if (latitude != 0.0 && longitude != 0.0) {
                            val shelterLocation = LatLng(latitude, longitude)
                            val distance = location.distanceExtention(shelterLocation)
                            distance <= 10000.0 // 10km = 10000m
                        } else {
                            false
                        }
                    }.sortedBy { shelter ->
                        // 가까운 대피소부터 정렬
                        val latitude = shelter.la?.toDoubleOrNull() ?: 0.0
                        val longitude = shelter.lo?.toDoubleOrNull() ?: 0.0
                        val shelterLocation = LatLng(latitude, longitude)
                        location.distanceExtention(shelterLocation)
                    }

                    // 필터링된 데이터로 대피소 업데이트
                    _shelters.value = filteredShelters
                    Log.d("OutdoorEvacuationViewModel_marker", "Shelters found within 5km: ${filteredShelters.size}")
                } ?: run {
                    // 주소 기반 필터링 개선 - 대략적인 위치 매칭 시도
                    val adminKeywords = currentAddress.split(" ")
                    val filteredByAddress = sheltersList.filter { shelter ->
                        val shelterAddress = shelter.eqkAcmdfcltyAdres ?: ""
                        // 주소의 일부라도 매칭되면 포함
                        adminKeywords.any { keyword ->
                            shelterAddress.contains(keyword) && keyword.length >= 2
                        }
                    }
                    _shelters.value = filteredByAddress
                    Log.d("OutdoorEvacuationViewModel_marker", "Shelters filtered by address: ${filteredByAddress.size}")
                }

            } catch (e: Exception) {
                Log.e("OutdoorEvacuationViewModel_error", "API request failed: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }


    // 위치 업데이트 메서드
    fun updateCurrentLocation(latitude: Double, longitude: Double) {
        _currentLocation.value = LatLng(latitude, longitude)
    }
}



