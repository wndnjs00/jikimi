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
import com.example.jikimi.data.model.dto.EarthquakeOutdoorsShelterResponse
import com.example.jikimi.databinding.FragmentEvacuateBinding
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Locale

@AndroidEntryPoint
class EvacuateFragment : Fragment(), OnMapReadyCallback {
    private val binding get() = _binding!!
    private var _binding: FragmentEvacuateBinding? = null

    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap

    private val viewModel: OutdoorEvacuationViewModel by viewModels()

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
//        observeViewModel()
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
    private fun observeViewModel(currentLocation: LatLng) {
        lifecycleScope.launchWhenCreated {
            viewModel.shelters.collect { shelters ->
                if (shelters.isNotEmpty()) {
                    // 대피소 데이터를 지도에 업데이트
                    updateMapWithShelters(shelters, currentLocation)

                    Toast.makeText(requireContext(), "${shelters.size} 개의 대피소를 찾았습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "대피소 데이터를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 대피소 데이터를 지도에 표시하고, 반경밖의 마커는 삭제
    private fun updateMapWithShelters(
        shelters: List<EarthquakeOutdoorsShelterResponse.EarthquakeOutdoorsShelter2.Row>,
        currentLocation: LatLng
    ) {
        // 기존마커 제거
        val marker = Marker()
        marker.map = null

        shelters.forEach { shelter ->
            val latitude = shelter.ycord?.toDoubleOrNull()?.let { String.format("%.7f", it).toDouble() } ?: 0.0
            val longitude = shelter.xcord?.toDoubleOrNull()?.let { String.format("%.7f", it).toDouble() } ?: 0.0

            // 좌표가 유효한 경우에만 마커 생성
            if (latitude != 0.0 && longitude != 0.0) {
                val shelterLocation = LatLng(latitude, longitude)
                val distance = calculateDistance(currentLocation, shelterLocation)

                if(distance <= 5000.0){
                    Marker().apply {
                        position = LatLng(latitude, longitude)
                        map = naverMap
                        icon = OverlayImage.fromResource(R.drawable.marker_red)
                        captionText = "${shelter.vtAcmdfcltyNm}"
                        captionRequestedWidth = 150
                    }
                }
            }
        }
    }


    // 두 지점 간의 거리 계산 (단위: 미터)
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

            // 반경 5km 서클오버레이 설정
            val circleOverlay = CircleOverlay()
            circleOverlay.map = null   //기존서클오버레이 제거
            circleOverlay.apply {
                center = LatLng(latitude, longitude)
                radius = 5000.0         // 반경 5km
                map = naverMap
                color = Color.argb(0, 255, 0, 0)    // 서클색상 투명으로
            }

            // Geocoder로 위경도를 주소로 변환
            val geocoder = Geocoder(requireContext(), Locale.KOREA)
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses?.isNotEmpty() == true) {
                val address = addresses[0]
                val ctprvnNm = address.adminArea ?: ""
//                val sggNm = address.locality ?: ""
                Log.d("현재주소","${ctprvnNm}")

                // API 호출
                viewModel.fetchAllShelters(ctprvnNm)
                observeViewModel(LatLng(latitude, longitude))
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

