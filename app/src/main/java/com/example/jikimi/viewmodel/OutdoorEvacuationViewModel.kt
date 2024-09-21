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
    val shelters : StateFlow<List<EarthquakeOutdoorsShelterResponse.EarthquakeOutdoorsShelter2.Row>> = _shelters


    // 특정 반경 내에 있는 대피소 데이터 가져오기
    fun getNearbyShelters(currentLocation: LatLng, radius: Double, arcd: String, ctprvnNm: String, sggNm: String) {
        viewModelScope.launch {
            try{
                val response = outdoorEvacuationRepository.requestOutdoorEvacuation(arcd,ctprvnNm,sggNm)
                val nearbyShelters = response?.earthquakeOutdoorsShelter2?.flatMap { it.row }?.filter{ shelter ->
                        // ycord와 xcord를 소숫점 7자리까지만 포맷팅
                        val latitude = shelter.ycord.toDoubleOrNull()?.let { String.format("%.7f", it).toDouble() } ?: 0.0
                        val longitude = shelter.xcord.toDoubleOrNull()?.let { String.format("%.7f", it).toDouble() } ?: 0.0
                        val shelterLocation = LatLng(latitude, longitude)
                        isWithinRadius(currentLocation, shelterLocation, radius)
                    } ?: emptyList()    // null인경우 빈 리스트로 처리

                _shelters.value = nearbyShelters
            }catch(e: Exception){
                // 에러처리
                Log.e("OutdoorEvacuationViewModel", "API 받아오기 실패: ${e.message}", e)
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
