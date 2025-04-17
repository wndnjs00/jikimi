package com.example.jikimi.presentation.fragment

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jikimi.R
import com.example.jikimi.data.local.dao.ShelterDao
import com.example.jikimi.data.model.dto.EarthquakeIndoorsShelterResponse
import com.example.jikimi.data.model.dto.EarthquakeOutdoorsShelterResponse
import com.example.jikimi.data.model.entity.ShelterEntity
import com.example.jikimi.data.network.distanceExtention
import com.example.jikimi.databinding.FragmentEvacuateBinding
import com.example.jikimi.presentation.adapter.ShelterSearchAdapter
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
import javax.inject.Inject
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

    private val searchAdapter = ShelterSearchAdapter { shelter ->
        onShelterSearchItemClick(shelter)
    }

    // SpeechRecognizer(음성인식) 관련 변수
    private lateinit var speechRecognizer: SpeechRecognizer
    private val RECORD_AUDIO_PERMISSION_CODE = 2000

    // Room DB 주입
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

        initializeMap()
        initializeLocationSource()
        likeBottomSheet()
        observeSharedViewModel()
        setupSearchUI()
        setupVoiceRecognition()
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
        // 위치 권한처리
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated) {
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }

        // 음성인식 권한처리
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 승인된 경우
                startVoiceRecognition()
            } else {
                // 권한이 거부된 경우
                Toast.makeText(
                    requireContext(),
                    "음성 인식을 사용하려면 마이크 권한이 필요합니다",
                    Toast.LENGTH_SHORT
                ).show()
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

                            // 검색을 위해 DB에 저장
                            saveOutdoorSheltersToDatabase(outdoorShelters)
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

                            // 검색을 위해 DB에 저장
                            saveIndoorSheltersToDatabase(indoorShelters)
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

                // 반경 5km 이내의 대피소만 표시
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

        // 선택된 위치에 마커 추가
        val marker = Marker().apply {
            position = LatLng(latitude, longitude)
            map = naverMap
            icon = OverlayImage.fromResource(
                if (shelterType == "임시주거시설") R.drawable.marker_blue else R.drawable.marker_red
            )
            captionText = shelterName
            captionRequestedWidth = 150
        }
    }


    // 검색을 위해
    // 야외 대피소를 Room DB에 저장 (중복체크 로직 추가)
    private fun saveOutdoorSheltersToDatabase(shelters: List<EarthquakeOutdoorsShelterResponse.Shelter>) {
        lifecycleScope.launch(Dispatchers.IO) {
            val shelterEntities = mutableListOf<ShelterEntity>()

            for (shelter in shelters) {
                val latitude = shelter.la?.toDoubleOrNull() ?: continue
                val longitude = shelter.lo?.toDoubleOrNull() ?: continue
                val name = shelter.vtAcmdfcltyNm ?: "이름 없음"

                // 중복 체크
                val existingShelter = shelterDao.findShelterByNameAndLocation(name, latitude, longitude)
                if (existingShelter == null) {
                    shelterEntities.add(
                        ShelterEntity(
                            vtAcmdfcltyNm = name,
                            address = shelter.eqkAcmdfcltyAdres ?: "주소 없음",
                            detailAddress = shelter.dtlAdres ?: "",
                            latitude = latitude,
                            longitude = longitude,
                            shelterType = "야외대피장소"
                        )
                    )
                }
            }

            if (shelterEntities.isNotEmpty()) {
                shelterDao.insertShelters(shelterEntities)
                Log.d("EvacuateFragment", "야외 대피소 ${shelterEntities.size}개를 저장했습니다.")
            }
        }
    }

    // 실내 대피소를 Room DB에 저장 (중복 체크 로직 추가)
    private fun saveIndoorSheltersToDatabase(shelters: List<EarthquakeIndoorsShelterResponse.EarthquakeIndoor.Row>) {
        lifecycleScope.launch(Dispatchers.IO) {
            val shelterEntities = mutableListOf<ShelterEntity>()

            for (shelter in shelters) {
                val latitude = shelter.ycord.toDoubleOrNull() ?: continue
                val longitude = shelter.xcord.toDoubleOrNull() ?: continue
                val name = shelter.vtAcmdfcltyNm ?: "이름 없음"

                // 중복 체크
                val existingShelter = shelterDao.findShelterByNameAndLocation(name, latitude, longitude)
                if (existingShelter == null) {
                    shelterEntities.add(
                        ShelterEntity(
                            vtAcmdfcltyNm = name,
                            address = shelter.rnAdres ?: "주소 없음",
                            detailAddress = shelter.dtlAdres ?: "",
                            latitude = latitude,
                            longitude = longitude,
                            shelterType = "임시주거시설"
                        )
                    )
                }
            }

            if (shelterEntities.isNotEmpty()) {
                shelterDao.insertShelters(shelterEntities)
                Log.d("EvacuateFragment", "실내 대피소 ${shelterEntities.size}개를 저장했습니다.")
            }
        }
    }



    // 검색 UI 설정
    private fun setupSearchUI() {
        // RecyclerView 설정
        binding.searchResultsRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        // 검색창 클릭 이벤트
        binding.searchEt.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {

                // 검색 UI 표시
                binding.searchOverlay.visibility = View.VISIBLE
                binding.searchContainer.visibility = View.VISIBLE

                // 초기에는 검색 결과를 비우고 안내 메시지 표시
                binding.noResultsTv.text = "검색어를 입력하세요"
                binding.noResultsTv.visibility = View.VISIBLE
                binding.searchResultsRv.visibility = View.GONE
                searchAdapter.updateShelters(emptyList())
            }
        }

        // 검색창 입력 이벤트
        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                if (query.isNotEmpty()) {
                    searchShelters(query)
                } else {
                    // 검색어가 비어있으면 결과 초기화
                    binding.noResultsTv.text = "검색어를 입력하세요"
                    binding.noResultsTv.visibility = View.VISIBLE
                    binding.searchResultsRv.visibility = View.GONE
                    searchAdapter.updateShelters(emptyList())
                }
            }
        })

        // 검색 완료 이벤트
        binding.searchEt.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                return@setOnEditorActionListener true
            }
            false
        }

        // 오버레이 클릭하면 검색 UI 숨기기
        binding.searchOverlay.setOnClickListener {
            hideSearchUI()
        }
    }

    // 키보드 숨기기
    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchEt.windowToken, 0)
    }

    // 검색 UI 숨기기
    private fun hideSearchUI() {
        binding.searchOverlay.visibility = View.GONE
        binding.searchContainer.visibility = View.GONE
        binding.searchEt.clearFocus()
        hideKeyboard()
    }

    // 검색 실행 (검색어가 있을 때만 검색)
    private fun searchShelters(query: String) {
        if (query.isEmpty()) {
            binding.noResultsTv.text = "검색어를 입력하세요"
            binding.noResultsTv.visibility = View.VISIBLE
            binding.searchResultsRv.visibility = View.GONE
            searchAdapter.updateShelters(emptyList())
            return
        }

        lifecycleScope.launch {
            shelterDao.searchShelters(query).collect { shelters ->
                updateSearchResults(shelters)
            }
        }
    }

    // 검색 결과 업데이트
    private fun updateSearchResults(shelters: List<ShelterEntity>) {
        if (shelters.isEmpty()) {
            binding.noResultsTv.text = "검색 결과가 없습니다"
            binding.noResultsTv.visibility = View.VISIBLE
            binding.searchResultsRv.visibility = View.GONE
        } else {
            binding.noResultsTv.visibility = View.GONE
            binding.searchResultsRv.visibility = View.VISIBLE
            searchAdapter.updateShelters(shelters)
        }
    }

    // 검색 결과 아이템 클릭 처리
    private fun onShelterSearchItemClick(shelter: ShelterEntity) {
        // 검색 UI 숨기기
        hideSearchUI()

        // 선택한 대피소로 카메라 이동
        moveCameraToLocation(shelter.latitude, shelter.longitude, shelter.vtAcmdfcltyNm, shelter.shelterType)

        // 마커 추가
        val markerPosition = LatLng(shelter.latitude, shelter.longitude)
        val marker = Marker().apply {
            position = markerPosition
            map = naverMap
            icon = OverlayImage.fromResource(
                if (shelter.shelterType == "임시주거시설") R.drawable.marker_blue else R.drawable.marker_red
            )
            captionText = shelter.vtAcmdfcltyNm
            captionRequestedWidth = 150
        }

        // 줌 레벨 조정
        val cameraUpdate = CameraUpdate.scrollAndZoomTo(markerPosition, 15.0)
            .animate(CameraAnimation.Easing)
        naverMap.moveCamera(cameraUpdate)
    }


    // SpeechRecognizer(음성인식) 초기화 및 설정
    private fun setupVoiceRecognition() {
        // SpeechRecognizer 초기화
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())

        // Intent 생성
        val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR") // 한국어 설정
            putExtra(RecognizerIntent.EXTRA_PROMPT, "음성으로 말해보세요")
        }

        // SpeechRecognizer 리스너 설정
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                // 음성 인식 준비 완료
                Toast.makeText(requireContext(), "음성 인식 준비 완료", Toast.LENGTH_SHORT).show()
            }

            override fun onBeginningOfSpeech() {
                // 음성 인식 시작
                Toast.makeText(requireContext(), "음성으로 말해보세요", Toast.LENGTH_SHORT).show()
            }

            override fun onRmsChanged(rmsdB: Float) {
                // 소리 크기 변경 (진폭)
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                // 버퍼 수신
            }

            override fun onEndOfSpeech() {
                // 음성 인식 종료
                Toast.makeText(requireContext(), "음성 인식 종료", Toast.LENGTH_SHORT).show()
            }

            override fun onError(error: Int) {
                // 오류 발생
                val message = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "오디오 에러"
                    SpeechRecognizer.ERROR_CLIENT -> "클라이언트 에러"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "권한 에러"
                    SpeechRecognizer.ERROR_NETWORK -> "네트워크 에러"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "네트워크 타임아웃"
                    SpeechRecognizer.ERROR_NO_MATCH -> "일치하는 결과 없음"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "음성인식기 사용 중"
                    SpeechRecognizer.ERROR_SERVER -> "서버 에러"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "음성 입력 시간 초과"
                    else -> "알 수 없는 에러"
                }
                Toast.makeText(requireContext(), "에러: $message", Toast.LENGTH_SHORT).show()
            }

            override fun onResults(results: Bundle?) {
                // 음성 인식 결과
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val recognizedText = matches[0] // 가장 신뢰도 높은 결과 사용

                    binding.searchEt.setText(recognizedText) // EditText에 결과 설정

                    // 검색 UI가 활성화되어 있는지 확인하고, 필요하면 활성화
                    if (binding.searchOverlay.visibility != View.VISIBLE) {
                        binding.searchOverlay.visibility = View.VISIBLE
                        binding.searchContainer.visibility = View.VISIBLE
                    }

                    // 검색 실행
                    if (recognizedText.isNotEmpty()) {
                        lifecycleScope.launch {
                            shelterDao.searchShelters(recognizedText).collect { shelters ->
                                updateSearchResults(shelters)
                            }
                        }
                    }
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                // 부분 결과
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                // 이벤트
            }
        })

        // EditText의 drawableEnd(음성 아이콘) 클릭 이벤트 처리
        binding.searchEt.setOnTouchListener { v, event ->
            val drawableEnd = 2 // drawableEnd의 인덱스는 2

            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (binding.searchEt.right - binding.searchEt.compoundDrawables[drawableEnd].bounds.width())) {
                    // 음성 인식 권한 확인 및 요청
                    checkVoiceRecognitionPermission()
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    // 음성인식 권한요청
    private fun checkVoiceRecognitionPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
        ) {
            // 권한이 없는 경우, 권한 요청
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_PERMISSION_CODE
            )
        } else {
            // 이미 권한이 있는 경우, 음성 인식 시작
            startVoiceRecognition()
        }
    }

    // 음성 인식 시작
    private fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR") // 한국어 설정
            putExtra(RecognizerIntent.EXTRA_PROMPT, "음성으로 말해보세요")
        }

        try {
            speechRecognizer.startListening(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "음성 인식 시작 실패: ${e.message}", Toast.LENGTH_SHORT).show()
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
        currentCircleOverlay?.map = null // 서클 오버레이 해제
        lastProcessedLocation = null // 메모리 해제
    }
}


