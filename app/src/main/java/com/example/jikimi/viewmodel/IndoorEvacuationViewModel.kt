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

    // API 요청 중인지 확인하는 플래그 추가
    private var isLoading = false

    // API데이터를 가져오고 업데이트
    // API데이터를 가져오고 업데이트 (중복 호출 방지 로직 추가)
    fun fetchIndoorShelters(currentAddress: String) {
        // 이미 로딩 중이면 중복 호출 방지
        if (isLoading) {
            Log.d("IndoorEvacuationViewModel", "Already loading data, skipping request")
            return
        }

        viewModelScope.launch {
            try {
                isLoading = true
                // API 호출
                val response = indoorEvacuationRepository.requestIndoorEvacuation()

                val sheltersList = response?.earthquakeIndoors?.flatMap { it.row ?: emptyList() } ?: emptyList()

                // currentLocation이 있는 경우만 거리 기반 필터링 수행
                _currentLocation.value?.let { location ->
                    // 반경 10km 이내의 대피소만 필터링
                    val filteredShelters = sheltersList.filter { shelter ->
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
                        val latitude = shelter.ycord?.toDoubleOrNull() ?: 0.0
                        val longitude = shelter.xcord?.toDoubleOrNull() ?: 0.0
                        val shelterLocation = LatLng(latitude, longitude)
                        location.distanceExtention(shelterLocation)
                    }

                    // 필터링한 대피소데이터를 shelters에 업데이트
                    _shelter.value = filteredShelters
                    Log.d("IndoorEvacuationViewModel", "Shelters found within 5km: ${filteredShelters.size}")
                } ?: run {
                    // currentLocation이 없는 경우 주소 기반으로 필터링
                    val filteredShelters = sheltersList.filter { it.sggNm == currentAddress }
                    _shelter.value = filteredShelters
                }

            } catch (e: Exception) {
                Log.e("IndoorEvacuationViewModel", "API 받아오기 실패: ${e.message}", e)
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

