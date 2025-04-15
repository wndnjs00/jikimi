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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.jikimi.R
import com.example.jikimi.data.model.dto.EarthquakeIndoorsShelterResponse
import com.example.jikimi.data.model.dto.EarthquakeOutdoorsShelterResponse
import com.example.jikimi.data.network.distanceExtention
import com.example.jikimi.databinding.FragmentEvacuateBinding
import com.example.jikimi.viewmodel.IndoorEvacuationViewModel
import com.example.jikimi.viewmodel.LikeSharedViewModel
import com.example.jikimi.viewmodel.OutdoorEvacuationViewModel
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.time.measureTimedValue

@AndroidEntryPoint
class EvacuateFragment : Fragment(), OnMapReadyCallback {
    private val binding get() = _binding!!
    private var _binding: FragmentEvacuateBinding? = null

    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap

    private val outdoorViewModel: OutdoorEvacuationViewModel by viewModels()
    private val indoorViewModel: IndoorEvacuationViewModel by viewModels()
    private val sharedViewModel : LikeSharedViewModel by activityViewModels()

    // 위치 업데이트 관련 변수 추가
    private var lastProcessedLocation: Location? = null
    private var lastApiCallTime: Long = 0
    private val MIN_DISTANCE_FOR_UPDATE = 100 // 100m 이상 이동 시 업데이트
    private val MIN_TIME_BETWEEN_UPDATES = 30000 // 30초 (밀리초 단위)

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
        likeBottomSheet()
        observeSharedViewModel()
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


    // ViewModel에서 위치 및 대피소 데이터 관찰
    private fun observeViewModels() {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                // 병렬로 수집
                launch{
                    // 야외 대피소 데이터 관찰
                    outdoorViewModel.shelters.collect { outdoorShelters ->
                        val currentLocation = outdoorViewModel.currentLocation.value
                        if (currentLocation != null && outdoorShelters.isNotEmpty()) {
                            updateOutdoorSheltersOnMap(outdoorShelters, currentLocation)
                            Toast.makeText(requireContext(), "${outdoorShelters.size} 개의 야외대피소를 찾았습니다.", Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(requireContext(), "야외대피소 데이터를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                launch {
                    // 실내 대피소 데이터 관찰
                    indoorViewModel.shelter.collect { indoorShelters ->
                        val currentLocation = indoorViewModel.currentLocation.value
                        if (currentLocation != null && indoorShelters.isNotEmpty()) {
                            updateIndoorSheltersOnMap(indoorShelters, currentLocation)
                            Toast.makeText(requireContext(), "${indoorShelters.size} 개의 실내 대피소를 찾았습니다.", Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(requireContext(), "실내대피소 데이터를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }


    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.locationSource = locationSource
        naverMap.uiSettings.isLocationButtonEnabled = true
        naverMap.locationTrackingMode = LocationTrackingMode.Follow

        // 마커 갱신을 호출하여 지도에 기존 마커를 다시 그림
        observeViewModels()

        // 위치가 변경될때마다 데이터 요청 (개선된 로직)
        naverMap.addOnLocationChangeListener { location ->
            // 현재 위치
            val currentLocation = Location("current").apply {
                latitude = location.latitude
                longitude = location.longitude
            }

            val currentTime = System.currentTimeMillis()

            // 이전 위치가 있고, 최소 거리 이동 조건과 최소 시간 경과 조건을 검사
            val shouldUpdate = lastProcessedLocation == null ||
                    (currentLocation.distanceTo(lastProcessedLocation!!) >= MIN_DISTANCE_FOR_UPDATE &&
                            currentTime - lastApiCallTime >= MIN_TIME_BETWEEN_UPDATES)

            // 즉시 위치 업데이트 (UI 업데이트용)
            outdoorViewModel.updateCurrentLocation(location.latitude, location.longitude)
            indoorViewModel.updateCurrentLocation(location.latitude, location.longitude)

            if (shouldUpdate) {
                Log.d("위치업데이트", "유의미한 위치 변경: ${location.latitude}, ${location.longitude}")

                // 위치 정보와 API 호출 시간 업데이트
                lastProcessedLocation = currentLocation
                lastApiCallTime = currentTime

                // Geocoder를 비동기적으로 실행
                lifecycleScope.launch {
                    val currentAddress = getCurrentAddress(location.latitude, location.longitude)

                    // currentAddress가 유효한 경우에만 API 요청
                    if (!currentAddress.isNullOrEmpty()) {
                        Log.d("위치변경_API_요청", "현재위치: ${location.latitude}, ${location.longitude}, 주소: $currentAddress")
                        outdoorViewModel.fetchOutdoorShelters(currentAddress)
                        indoorViewModel.fetchIndoorShelters(currentAddress)
                    } else {
                        // 주소 변환 실패시에도 위치 기반으로만 데이터 요청
                        outdoorViewModel.fetchOutdoorShelters("")
                        indoorViewModel.fetchIndoorShelters("")
                    }
                }
            }

            // 반경 5km 서클오버레이 설정 (UI 업데이트용이므로 항상 실행)
            updateCircleOverlay(location.latitude, location.longitude)
        }
    }

    // 서클 오버레이 업데이트 메서드 분리 (코드 가독성 향상)
    private var currentCircleOverlay: CircleOverlay? = null
    private fun updateCircleOverlay(latitude: Double, longitude: Double) {
        // 기존 서클 제거
        currentCircleOverlay?.map = null

        // 새로운 서클 생성 및 표시
        currentCircleOverlay = CircleOverlay().apply {
            center = LatLng(latitude, longitude)
            radius = 5000.0     // 반경 5km
            map = naverMap
            color = Color.argb(50, 255, 0, 0) // 투명한 색상 설정
        }
    }


    // Geocoder를 사용해 위경도 좌표를 주소로 변환 (백그라운드에서 처리)
    private suspend fun getCurrentAddress(latitude: Double, longitude: Double): String? {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(requireContext(), Locale.KOREA)
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)

                if (addresses?.isNotEmpty() == true) {
                    // 시/도 정보 (예: 경기도)와 시/군/구 정보 (예: 수원시) 결합
                    val adminArea = addresses[0].adminArea ?: "" // 시/도 (예: 경기도)
                    val locality = addresses[0].locality ?: ""   // 시/군/구 (예: 수원시)

                    val current_address = if (locality.isNotEmpty()) {
                        "$adminArea $locality"
                    } else {
                        adminArea
                    }

                    Log.d("현재주소", "$current_address")
                    current_address
                } else {
                    Log.e("현재주소_에러", "현재주소를 찾을 수 없습니다.")
                    null
                }
            } catch (e: Exception) {
                Log.e("현재주소_에러", "주소변환 중 오류: ${e.message}")
                null
            }
        }
    }


    // 야외대피소 데이터를 지도에 표시하고, 반경밖의 마커는 삭제
    private fun updateOutdoorSheltersOnMap(
        shelters: List<EarthquakeOutdoorsShelterResponse.Shelter>,
        currentLocation: LatLng
    ) {
        // 기존 마커 제거
        val marker = Marker()
        marker.map = null

        shelters.forEach { outdoorShelter ->
            val latitude = outdoorShelter.la?.toDoubleOrNull()?.let { String.format("%.7f", it).toDouble() } ?: 0.0
            val longitude = outdoorShelter.lo?.toDoubleOrNull()?.let { String.format("%.7f", it).toDouble() } ?: 0.0

            // 유효한 좌표인지 확인
            if (latitude != 0.0 && longitude != 0.0) {
                val shelterLocation = LatLng(latitude, longitude)
                val distance = currentLocation.distanceExtention(shelterLocation)

                // 반경 5km 이내의 대피소만 표시
                if (distance <= 5000.0) {
                    val outdoorMarker = Marker().apply {
                        position = LatLng(latitude, longitude)
                        map = naverMap
                        icon = OverlayImage.fromResource(R.drawable.marker_red)
                        captionText = "${outdoorShelter.vtAcmdfcltyNm}\n${String.format("%.2f", distance)} m"
                        captionRequestedWidth = 150
                    }

                    outdoorMarker.setOnClickListener {
                        // 마커 클릭시 BottomSheetFragment로 Row전체데이터(outdoorShelter)와 distance 전달
                        val bottomSheetFragment = BottomSheetFragment.outdoorNewInstance(outdoorShelter, distance)

                        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
                        Toast.makeText(requireContext(), "${outdoorShelter.vtAcmdfcltyNm} 클릭됨", Toast.LENGTH_SHORT).show()
                        true
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
        // 기존 마커 제거
        val marker = Marker()
        marker.map = null

        shelters.forEach { indoorShelter ->
            val latitude = indoorShelter.ycord.toDoubleOrNull()?.let { String.format("%.7f", it).toDouble() } ?: 0.0
            val longitude = indoorShelter.xcord.toDoubleOrNull()?.let { String.format("%.7f", it).toDouble() } ?: 0.0

            // 유효한 좌표인지 확인
            if (latitude != 0.0 && longitude != 0.0) {
                val shelterLocation = LatLng(latitude, longitude)
                val distance = currentLocation.distanceExtention(shelterLocation)

                // 반경 5km 이내의 대피소만 표시 (5000m)
                if (distance <= 5000.0) {
                    val indoorMarker = Marker().apply {
                        position = LatLng(latitude, longitude)
                        map = naverMap
                        icon = OverlayImage.fromResource(R.drawable.marker_blue)
                        captionText = "${indoorShelter.vtAcmdfcltyNm}\n${String.format("%.2f", distance)} m"
                        captionRequestedWidth = 150
                    }

                    indoorMarker.setOnClickListener {
                        // 마커 클릭시 BottomSheetFragment로 Row전체데이터(indoorShelter)와 distance 전달
                        val bottomSheetFragment = BottomSheetFragment.indoorNewInstance(indoorShelter, distance)

                        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
                        Toast.makeText(requireContext(), "${indoorShelter.vtAcmdfcltyNm} 클릭됨", Toast.LENGTH_SHORT).show()
                        true
                    }
                }
            }
        }
    }

    // likeConstraint 눌렀을때 bottomSheet 나오게
    private fun likeBottomSheet(){
        binding.likeConstraint.setOnClickListener {
            val likeBottomSheetFragment = LikeBottomSheetFragment()
            likeBottomSheetFragment.show(childFragmentManager, likeBottomSheetFragment.tag)
        }
    }


    // sharedViewModel로 데이터공유
    private fun observeSharedViewModel() {
        lifecycleScope.launch {
            sharedViewModel.selectedLikeEntity.collect { likeEntity ->
                likeEntity?.let {
                    moveCameraToLocation(it.latitude, it.longitude, it.vtAcmdfcltyNm, it.shelterType)
                }
            }
        }
    }



    // 네이버맵 카메라이동 + 마커표시함수
    fun moveCameraToLocation(latitude: Double, longitude: Double, shelterName: String, shelterType: String) {
            val cameraUpdate = CameraUpdate.scrollTo(LatLng(latitude, longitude)).animate(CameraAnimation.Easing)
            naverMap.moveCamera(cameraUpdate)

        // 마커가 없을 경우 새로 추가
//        if (marker == null) {
//            marker = Marker().apply {
//                position = LatLng(latitude, longitude)
//                map = naverMap
//                captionText = shelterName
//                icon = when(shelterType){
//                    "임시주거시설" -> OverlayImage.fromResource(R.drawable.marker_blue)  // 임시주거시설이면 파란 아이콘
//                    "야외대피장소" -> OverlayImage.fromResource(R.drawable.marker_red)
//                    else -> OverlayImage.fromResource(R.drawable.ic_launcher_foreground)
//                }
//                captionRequestedWidth = 150
//            }
//        } else {
//            // 마커가 이미 있으면 위치 업데이트
//            marker?.position = LatLng(latitude, longitude)
//        }
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
        currentCircleOverlay?.map = null // 서클 오버레이 해제
        lastProcessedLocation = null // 메모리 해제
    }
}


