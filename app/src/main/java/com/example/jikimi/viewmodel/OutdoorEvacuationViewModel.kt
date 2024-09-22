package com.example.jikimi.viewmodel

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jikimi.data.model.dto.EarthquakeOutdoorsShelterResponse
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

    private val _shelters = MutableStateFlow<List<EarthquakeOutdoorsShelterResponse.EarthquakeOutdoorsShelter2.Row>>(emptyList())
    val shelters: StateFlow<List<EarthquakeOutdoorsShelterResponse.EarthquakeOutdoorsShelter2.Row>> = _shelters


    // 전체 데이터를 호출하고 사용자의 현재 위치를 기준으로 반경 내 대피소만 필터링
    fun fetchAllSheltersAndFilterByLocation(currentLocation: LatLng, radius: Double) {
        viewModelScope.launch {
            try {
                // API 호출
                val response = outdoorEvacuationRepository.requestOutdoorEvacuation()

                // API 응답결과 로그
                Log.d("OutdoorEvacuationViewModel_response", "Response received: $response")

                // earthquakeOutdoorsShelter2, row가 null일 경우에 emptyList로 처리하여 null값 받지않도록 수정
                val nearbyShelters = response?.earthquakeOutdoorsShelter2?.flatMap { it.row ?: emptyList() }?.filter { shelter ->
                    val latitude = shelter.ycord?.toDoubleOrNull()?.let { String.format("%.7f", it).toDouble() }
                    val longitude = shelter.xcord?.toDoubleOrNull()?.let { String.format("%.7f", it).toDouble() }

                    // 좌표가 null인 경우 필터링
                    if (latitude != null && longitude != null) {
                        val shelterLocation = LatLng(latitude, longitude)

                        // 좌표 로그 확인
                        Log.d("ShelterLocation", "Latitude: $latitude, Longitude: $longitude")

                        // 반경 내에 있는지 확인
                        isWithinRadius(currentLocation, shelterLocation, radius)
                    } else {
                        Log.e("InvalidCoordinates", "Shelter has invalid or missing coordinates: $shelter")
                        false
                    }
                } ?: emptyList() // null인 경우 빈 리스트로 처리

                // 유효한 대피소가 있을 때만 shelters 업데이트
                _shelters.value = nearbyShelters

                // 마커가 업데이트되었음을 로그로 표시
                Log.d("OutdoorEvacuationViewModel_marker", "Nearby shelters found: ${nearbyShelters.size}")
            } catch (e: Exception) {
                // 에러 발생 시 로그로 출력
                Log.e("OutdoorEvacuationViewModel_error", "API 받아오기 실패: ${e.message}", e)
            }
        }
    }

    // 반경 내에 있는지 여부를 판단하는 함수
    private fun isWithinRadius(currentLocation: LatLng, shelterLocation: LatLng, radius: Double): Boolean {
        val distance = FloatArray(1)
        Location.distanceBetween(
            currentLocation.latitude, currentLocation.longitude,
            shelterLocation.latitude, shelterLocation.longitude,
            distance
        )
        return distance[0] <= radius
    }
}
