package com.example.jikimi.presentation.fragment

import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.jikimi.R
import com.example.jikimi.data.model.dto.EarthquakeIndoorsShelterResponse
import com.example.jikimi.data.model.dto.EarthquakeOutdoorsShelterResponse
import com.example.jikimi.databinding.FragmentEvacuateBinding
import com.example.jikimi.viewmodel.IndoorEvacuationViewModel
import com.example.jikimi.viewmodel.OutdoorEvacuationViewModel
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.CircleOverlay
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

@AndroidEntryPoint
class EvacuateFragment : Fragment(), OnMapReadyCallback {
    private val binding get() = _binding!!
    private var _binding: FragmentEvacuateBinding? = null

    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap

    private val outdoorViewModel: OutdoorEvacuationViewModel by viewModels()
    private val indoorViewModel: IndoorEvacuationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEvacuateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeMap()
        initializeLocationSource()
    }

    // 지도 초기화
    private fun initializeMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                childFragmentManager.beginTransaction().add(R.id.map, it).commit()
            }
        mapFragment.getMapAsync(this)
    }

    // FusedLocationSource 초기화
    private fun initializeLocationSource() {
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated) {
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    // ViewModel의 데이터를 관찰하고 대피소 마커 표시
    private fun observeOutdoorViewModel(currentLocation: LatLng) {
        lifecycleScope.launchWhenCreated {
            outdoorViewModel.shelters.collect { outdoorShelters ->
                if (outdoorShelters.isNotEmpty()) {
                    // 대피소 데이터를 지도에 업데이트
                    updateOutdoorSheltersOnMap(outdoorShelters, currentLocation)
                    Toast.makeText(requireContext(), "${outdoorShelters.size} 개의 야외대피소를 찾았습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "야외대피소 데이터를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // indoorViewModel 데이터 관찰 및 마커 표시
    private fun observeIndoorViewModel(currentLocation: LatLng) {
        lifecycleScope.launchWhenCreated {
            indoorViewModel.shelter.collect { indoorShelters ->
                if (indoorShelters.isNotEmpty()) {
                    // 대피소 데이터를 지도에 업데이트
                    updateIndoorSheltersOnMap(indoorShelters, currentLocation)
                    Toast.makeText(requireContext(), "${indoorShelters.size} 개의 실내 대피소를 찾았습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "실내대피소 데이터를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    // 야외대피소 데이터를 지도에 표시하고, 반경밖의 마커는 삭제
    private fun updateOutdoorSheltersOnMap(
        shelters: List<EarthquakeOutdoorsShelterResponse.EarthquakeOutdoorsShelter2.Row>,
        currentLocation: LatLng
    ) {
        // 기존마커 제거
        val marker = Marker()
        marker.map = null

        shelters.forEach { outdoorShelter ->
            val latitude = outdoorShelter.ycord?.toDoubleOrNull()?.let { String.format("%.7f", it).toDouble() } ?: 0.0
            val longitude = outdoorShelter.xcord?.toDoubleOrNull()?.let { String.format("%.7f", it).toDouble() } ?: 0.0

            // 좌표가 유효한 경우에만 마커 생성
            if (latitude != 0.0 && longitude != 0.0) {
                val shelterLocation = LatLng(latitude, longitude)
                val distance = calculateDistance(currentLocation, shelterLocation)

                if(distance <= 10000.0){
                    Marker().apply {
                        position = LatLng(latitude, longitude)
                        map = naverMap
                        icon = OverlayImage.fromResource(R.drawable.marker_red)
                        captionText = "${outdoorShelter.vtAcmdfcltyNm}\n${String.format("%.2f", distance)} m"
                        captionRequestedWidth = 150
                    }
                }
            }
        }
    }


    // 실내대피소 데이터를 지도에 표시하고, 반경밖의 마커는 삭제
    private fun updateIndoorSheltersOnMap(
        shelters: List<EarthquakeIndoorsShelterResponse.EarthquakeIndoor.Row>,
        currentLocation: LatLng
    ) {
        val marker = Marker()
        marker.map = null

        shelters.forEach { indoorShelter ->
            val latitude = indoorShelter.ycord?.toDoubleOrNull()?.let { String.format("%.7f", it).toDouble() } ?: 0.0
            val longitude = indoorShelter.xcord?.toDoubleOrNull()?.let { String.format("%.7f", it).toDouble() } ?: 0.0

            if (latitude != 0.0 && longitude != 0.0) {
                val shelterLocation = LatLng(latitude, longitude)
                val distance = calculateDistance(currentLocation, shelterLocation)

                if (distance <= 10000.0) {
                    Marker().apply {
                        position = LatLng(latitude, longitude)
                        map = naverMap
                        icon = OverlayImage.fromResource(R.drawable.maker_blue)
                        captionText = "${indoorShelter.vtAcmdfcltyNm}\n${String.format("%.2f", distance)} m"
                        captionRequestedWidth = 150
                    }
                }
            }
        }
    }


    // 두지점간의 거리 계산
    private fun calculateDistance(start: LatLng, end: LatLng): Double {
        val results = FloatArray(1)
        Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, results)
        return results[0].toDouble()
    }


    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.locationSource = locationSource
        naverMap.uiSettings.isLocationButtonEnabled = true
        naverMap.locationTrackingMode = LocationTrackingMode.Follow

        // 위치가 변경될때마다 데이터 요청
        naverMap.addOnLocationChangeListener { location ->
            // 현재위치 받아옴
            val latitude = location.latitude
            val longitude = location.longitude

            // 반경 10km 서클오버레이 설정
            CircleOverlay().apply {
                map = null  // 기존 서클 오버레이 제거
                center = LatLng(latitude, longitude)
                radius = 10000.0     // 반경 10km
                map = naverMap
                color = Color.argb(50, 255, 0, 0) // 투명한 색상 설정(알파0으로 바꾸기)
            }

            // Geocoder를 비동기적으로 실행
            lifecycleScope.launch {
                val currentAddress = getCurrentAddress(latitude, longitude)

                // currentAddress가 유효한 경우에만 API 요청
                if (!currentAddress.isNullOrEmpty()) {
                    outdoorViewModel.fetchOutdoorShelters(currentAddress)
                    indoorViewModel.fetchIndoorShelters(currentAddress)

                    // ViewModel 데이터 관찰
                    observeOutdoorViewModel(LatLng(latitude, longitude))
                    observeIndoorViewModel(LatLng(latitude, longitude))
                }
            }
        }
    }


    // Geocoder를 사용해 위경도 좌표를 주소로 변환 (백그라운드에서 처리)
    private suspend fun getCurrentAddress(latitude: Double, longitude: Double): String? {
        return withContext(Dispatchers.IO) {
                val geocoder = Geocoder(requireContext(), Locale.KOREA)
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)

                if (addresses?.isNotEmpty() == true) {
                    val current_address = addresses[0].adminArea // adminArea에 해당하는 ctprvnNm 반환
                    Log.d("현재주소", "$current_address") // 로그로 출력
                    current_address
                } else {
                    Log.e("현재주소_에러", "현재주소를 찾을 수 없습니다.")
                    null
                }
            }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        // naverMap 관련 리소스를 해제
        naverMap.locationSource = null // LocationSource 해제
        val marker = Marker()
        marker.map = null
    }
}

