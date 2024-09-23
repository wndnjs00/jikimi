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

    // API로 ctprvnNm,sggNm데이터를 가져오고 업데이트
    fun fetchAllShelters(ctprvnNm: String) {
        viewModelScope.launch {
            try {
                // ctprvnNm를 기반으로 API 호출
                val response = outdoorEvacuationRepository.requestOutdoorEvacuation(ctprvnNm)
                Log.d("OutdoorEvacuationViewModel_response", "Response received: $response")

                // earthquakeOutdoorsShelter2의 row가 null일 경우 emptyList로 처리하여 null값 받지 않도록
                val sheltersList = response?.earthquakeOutdoorsShelter2?.flatMap { it.row ?: emptyList() } ?: emptyList()

                // 가져온 대피소데이터를 shelters에 업데이트
                _shelters.value = sheltersList
                Log.d("OutdoorEvacuationViewModel_marker", "Shelters found: ${sheltersList.size}")

            } catch (e: Exception) {
                Log.e("OutdoorEvacuationViewModel_error", "API 받아오기 실패: ${e.message}", e)
            }
        }
    }
}
