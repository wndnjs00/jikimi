package com.myapp.jikimi.presentation.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.myapp.jikimi.R
import com.myapp.jikimi.data.local.dao.ShelterDao
import com.myapp.jikimi.data.model.dto.EarthquakeIndoorsShelterResponse
import com.myapp.jikimi.data.model.dto.EarthquakeOutdoorsShelterResponse
import com.myapp.jikimi.data.model.entity.ShelterEntity
import com.myapp.jikimi.data.network.LOCATION_PERMISSION_REQUEST_CODE
import com.myapp.jikimi.data.network.MIN_DISTANCE_FOR_UPDATE
import com.myapp.jikimi.data.network.MIN_TIME_BETWEEN_UPDATES
import com.myapp.jikimi.data.network.RECORD_AUDIO_PERMISSION_CODE
import com.myapp.jikimi.data.network.haversineDistance
import com.myapp.jikimi.databinding.FragmentEvacuateBinding
import com.myapp.jikimi.presentation.utils.ShelterSearchManager
import com.myapp.jikimi.presentation.utils.VoiceRecognitionManager
import com.myapp.jikimi.presentation.utils.showToast
import com.myapp.jikimi.viewmodel.IndoorEvacuationViewModel
import com.myapp.jikimi.viewmodel.OutdoorEvacuationViewModel
import com.myapp.jikimi.viewmodel.SharedViewModel
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
import javax.inject.Inject

@AndroidEntryPoint
class EvacuateFragment : Fragment(), OnMapReadyCallback {
    private val binding get() = _binding!!
    private var _binding: FragmentEvacuateBinding? = null
    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap
    private val outdoorViewModel: OutdoorEvacuationViewModel by viewModels()
    private val indoorViewModel: IndoorEvacuationViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()

    // 위치 업데이트 관련 변수
    private var lastProcessedLocation: Location? = null
    private var lastApiCallTime: Long = 0

    // 매니저 클래스들
    private lateinit var voiceRecognitionManager: VoiceRecognitionManager
    private lateinit var shelterSearchManager: ShelterSearchManager
    private var currentCircleOverlay: CircleOverlay? = null

    @Inject
    lateinit var shelterDao: ShelterDao

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEvacuateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeComponents()
        setupObservers()
    }

    private fun initializeComponents() {
        initializeMap()
        initializeLocationSource()
        initializeManagers()
        likeBottomSheet()
    }

    private fun initializeMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                childFragmentManager.beginTransaction().add(R.id.map, it).commit()
            }
        mapFragment.getMapAsync(this)
    }

    private fun initializeLocationSource() {
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
    }

    private fun initializeManagers() {
        // 음성인식 초기화
        voiceRecognitionManager = VoiceRecognitionManager(requireContext()) { recognizedText ->
            shelterSearchManager.performVoiceSearch(recognizedText)
        }
        voiceRecognitionManager.initialize()

        // 검색 초기화
        shelterSearchManager = ShelterSearchManager(
            binding = binding,
            shelterDao = shelterDao,
            onShelterSelected = { shelter ->
                onShelterSearchItemClick(shelter)
            }
        )
        shelterSearchManager.setupSearchUI()
        shelterSearchManager.setupVoiceSearchIcon {
            checkVoiceRecognitionPermission()
        }
    }

    private fun setupObservers() {
        observeViewModels()
        observeSharedViewModel()
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        // 위치 권한 처리
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated) {
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }

        // 음성인식 권한 처리
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                voiceRecognitionManager.startListening()
            } else {
                requireContext().showToast("음성 인식을 사용하려면 마이크 권한이 필요합니다")
            }
            return
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun observeViewModels() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 야외 대피소 관찰
                launch {
                    outdoorViewModel.isLoading.collect { isLoading ->
                        binding.progressBar.isVisible = isLoading
                    }
                }
                launch {
                    outdoorViewModel.errorMessage.collect { errorMessage ->
                        if (!errorMessage.isNullOrEmpty()) {
                            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                            outdoorViewModel.clearErrorMessage()
                        }
                    }
                }
                launch {
                    outdoorViewModel.shelters.collect { outdoorShelters ->
                        val currentLocation = outdoorViewModel.currentLocation.value
                        if (currentLocation != null && outdoorShelters.isNotEmpty()) {
                            updateOutdoorSheltersOnMap(outdoorShelters, currentLocation)
                            requireContext().showToast("${outdoorShelters.size}개의 야외대피소를 찾았습니다.")
                        }
                    }
                }
                // 실내 대피소 관찰
                launch {
                    indoorViewModel.isLoading.collect { isLoading ->
                        binding.progressBar2.isVisible = isLoading
                    }
                }
                launch {
                    indoorViewModel.errorMessage.collect { errorMessage ->
                        if (!errorMessage.isNullOrEmpty()) {
                            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                            indoorViewModel.clearErrorMessage()
                        }
                    }
                }
                launch {
                    indoorViewModel.shelter.collect { indoorShelters ->
                        val currentLocation = indoorViewModel.currentLocation.value
                        if (currentLocation != null && indoorShelters.isNotEmpty()) {
                            updateIndoorSheltersOnMap(indoorShelters, currentLocation)
                            requireContext().showToast("${indoorShelters.size} 개의 실내 대피소를 찾았습니다.")
                        }
                    }
                }
            }
        }
    }

    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.locationSource = locationSource
        naverMap.uiSettings.isLocationButtonEnabled = true
        naverMap.locationTrackingMode = LocationTrackingMode.Follow
        // 위치 변경 리스너 설정
        naverMap.addOnLocationChangeListener { location ->
            handleLocationChange(location)
        }
    }

    private fun handleLocationChange(location: Location) {
        val currentLocation = Location("current").apply {
            latitude = location.latitude
            longitude = location.longitude
        }
        val currentTime = System.currentTimeMillis()
        val shouldUpdate = lastProcessedLocation == null ||
                (currentLocation.distanceTo(lastProcessedLocation!!) >= MIN_DISTANCE_FOR_UPDATE &&
                        currentTime - lastApiCallTime >= MIN_TIME_BETWEEN_UPDATES)
        // 즉시 위치 업데이트 (UI 업데이트용)
        outdoorViewModel.updateCurrentLocation(location.latitude, location.longitude)
        indoorViewModel.updateCurrentLocation(location.latitude, location.longitude)
        if (shouldUpdate) {
            Log.d("위치업데이트", "유의미한 위치 변경: ${location.latitude}, ${location.longitude}")
            lastProcessedLocation = currentLocation
            lastApiCallTime = currentTime

            // 주소 기반 API 요청
            lifecycleScope.launch {
                val currentAddress = getCurrentAddress(location.latitude, location.longitude)
                if (!currentAddress.isNullOrEmpty()) {
                    Log.d(
                        "위치변경_API_요청",
                        "현재위치: ${location.latitude}, ${location.longitude}, 주소: $currentAddress"
                    )
                    outdoorViewModel.fetchOutdoorShelters(currentAddress)
                    indoorViewModel.fetchIndoorShelters(currentAddress)
                } else {
                    outdoorViewModel.fetchOutdoorShelters("")
                    indoorViewModel.fetchIndoorShelters("")
                }
            }
        }
        updateCircleOverlay(location.latitude, location.longitude)
    }

    private fun updateCircleOverlay(latitude: Double, longitude: Double) {
        currentCircleOverlay?.map = null
        currentCircleOverlay = CircleOverlay().apply {
            center = LatLng(latitude, longitude)
            radius = 5000.0
            map = naverMap
            color = Color.argb(0, 128, 0, 128)
        }
    }

    private suspend fun getCurrentAddress(latitude: Double, longitude: Double): String? {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(requireContext(), Locale.KOREA)
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)

                if (addresses?.isNotEmpty() == true) {
                    val adminArea = addresses[0].adminArea ?: ""
                    val locality = addresses[0].locality ?: ""
                    val currentAddress = if (locality.isNotEmpty()) {
                        "$adminArea $locality"
                    } else {
                        adminArea
                    }
                    Log.d("현재주소", currentAddress)
                    currentAddress
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

    private fun updateOutdoorSheltersOnMap(
        shelters: List<EarthquakeOutdoorsShelterResponse.Shelter>,
        currentLocation: LatLng
    ) {
        val marker = Marker()
        marker.map = null

        shelters.forEach { outdoorShelter ->
            val latitude = outdoorShelter.outdoorLongitude?.toDoubleOrNull()
                ?.let { String.format("%.7f", it).toDouble() } ?: 0.0
            val longitude = outdoorShelter.outdoorLatitude?.toDoubleOrNull()
                ?.let { String.format("%.7f", it).toDouble() } ?: 0.0

            if (latitude != 0.0 && longitude != 0.0) {
                val shelterLocation = LatLng(latitude, longitude)
                val distance = currentLocation.haversineDistance(shelterLocation)

                if (distance <= 5000.0) {
                    val outdoorMarker = Marker().apply {
                        position = LatLng(latitude, longitude)
                        map = naverMap
                        icon = OverlayImage.fromResource(R.drawable.marker_red)
                        captionText = "${outdoorShelter.outdoorShelterName}\n${
                            String.format(
                                "%.2f",
                                distance
                            )
                        } m"
                        captionRequestedWidth = 150
                    }

                    outdoorMarker.setOnClickListener {
                        val bottomSheetFragment =
                            BottomSheetFragment.outdoorNewInstance(outdoorShelter, distance)
                        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
                        true
                    }
                }
            }
        }
    }

    private fun updateIndoorSheltersOnMap(
        shelters: List<EarthquakeIndoorsShelterResponse.EarthquakeIndoor.Row>,
        currentLocation: LatLng
    ) {
        val marker = Marker()
        marker.map = null

        shelters.forEach { indoorShelter ->
            val latitude = indoorShelter.indoorLongitude.toDoubleOrNull()
                ?.let { String.format("%.7f", it).toDouble() } ?: 0.0
            val longitude = indoorShelter.indoorLatitude.toDoubleOrNull()
                ?.let { String.format("%.7f", it).toDouble() } ?: 0.0

            if (latitude != 0.0 && longitude != 0.0) {
                val shelterLocation = LatLng(latitude, longitude)
                val distance = currentLocation.haversineDistance(shelterLocation)

                if (distance <= 5000.0) {
                    val indoorMarker = Marker().apply {
                        position = LatLng(latitude, longitude)
                        map = naverMap
                        icon = OverlayImage.fromResource(R.drawable.marker_blue)
                        captionText = "${indoorShelter.indoorShelterName}\n${
                            String.format(
                                "%.2f",
                                distance
                            )
                        } m"
                        captionRequestedWidth = 150
                    }

                    indoorMarker.setOnClickListener {
                        val bottomSheetFragment =
                            BottomSheetFragment.indoorNewInstance(indoorShelter, distance)
                        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
                        true
                    }
                }
            }
        }
    }

    private fun likeBottomSheet() {
        binding.likeConstraint.setOnClickListener {
            val likeBottomSheetFragment = LikeBottomSheetFragment()
            likeBottomSheetFragment.show(childFragmentManager, likeBottomSheetFragment.tag)
        }
    }

    private fun observeSharedViewModel() {
        lifecycleScope.launch {
            sharedViewModel.selectedLikeEntity.collect { likeEntity ->
                likeEntity?.let {
                    moveCameraToLocation(it.latitude, it.longitude, it.shelterName, it.shelterType)
                }
            }
        }
    }

    fun moveCameraToLocation(
        latitude: Double,
        longitude: Double,
        shelterName: String,
        shelterType: String
    ) {
        val cameraUpdate =
            CameraUpdate.scrollTo(LatLng(latitude, longitude)).animate(CameraAnimation.Easing)
        naverMap.moveCamera(cameraUpdate)
        Marker().apply {
            position = LatLng(latitude, longitude)
            map = naverMap
            icon = OverlayImage.fromResource(
                if (shelterType == "임시주거시설") R.drawable.marker_blue else R.drawable.marker_red
            )
            captionText = shelterName
            captionRequestedWidth = 150
        }
    }

    private fun onShelterSearchItemClick(shelter: ShelterEntity) {
        shelterSearchManager.hideSearchUI()
        moveCameraToLocation(
            shelter.latitude,
            shelter.longitude,
            shelter.shelterName,
            shelter.shelterType
        )

        val markerPosition = LatLng(shelter.latitude, shelter.longitude)
        Marker().apply {
            position = markerPosition
            map = naverMap
            icon = OverlayImage.fromResource(
                if (shelter.shelterType == "임시주거시설") R.drawable.marker_blue else R.drawable.marker_red
            )
            captionText = shelter.shelterName
            captionRequestedWidth = 150
        }

        val cameraUpdate = CameraUpdate.scrollAndZoomTo(markerPosition, 15.0)
            .animate(CameraAnimation.Easing)
        naverMap.moveCamera(cameraUpdate)
    }

    private fun checkVoiceRecognitionPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_PERMISSION_CODE
            )
        } else {
            voiceRecognitionManager.startListening()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        voiceRecognitionManager.destroy()
        _binding = null
        val marker = Marker()
        marker.map = null
        currentCircleOverlay?.map = null
        lastProcessedLocation = null
    }
}