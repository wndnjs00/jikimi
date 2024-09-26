package com.example.jikimi.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jikimi.data.model.dto.EarthquakeIndoorsShelterResponse
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


    // API데이터를 가져오고 업데이트
    fun fetchIndoorShelters(currentAddress: String) {
        viewModelScope.launch {
            try {
                // API 호출
                val response = indoorEvacuationRepository.requestIndoorEvacuation()
                Log.d("IndoorEvacuationViewModel_response", "Response received: $response")

                val sheltersList = response?.earthquakeIndoors?.flatMap { it.row ?: emptyList() } ?: emptyList()
                // ctprvnNm(지역명) 필터링 [전체데이터중에서 지역명과 일치하는 지역만 보여주기위해서]
                val filteredShelters = sheltersList.filter { it.ctprvnNm == currentAddress }

                // 필터링한 대피소데이터를 shelters에 업데이트
                _shelter.value = filteredShelters
                Log.d("IndoorEvacuationViewModel_marker", "Shelters found: ${filteredShelters.size}")

            } catch (e: Exception) {
                Log.e("IndoorEvacuationViewModel_error", "API 받아오기 실패: ${e.message}", e)
            }
        }
    }


    // 위치 업데이트 메서드
    fun updateCurrentLocation(latitude: Double, longitude: Double) {
        _currentLocation.value = LatLng(latitude, longitude)
    }
}

