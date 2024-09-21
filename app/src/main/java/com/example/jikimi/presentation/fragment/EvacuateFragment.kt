package com.example.jikimi.presentation.fragment

import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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
        observeViewModel()
    }


    // 지도 초기화
    private fun initializeMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                childFragmentManager.beginTransaction().add(R.id.map, it).commit()
            }
        // 지도가 준비되었을 때 onMapReady 콜백 받도록
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
            // 권한 거부한경우
            if (!locationSource.isActivated) {
                naverMap.locationTrackingMode = LocationTrackingMode.None //위치추적 비활성화
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    // 대피소 데이터 관찰하여 대피소 마커표시
    private fun observeViewModel(){
        lifecycleScope.launchWhenCreated {
            viewModel.shelters.collect{
                updateMapWithShelters(it)
            }
        }
    }


//    private fun observeViewModel(){
//        viewLifecycleOwner.lifecycleScope.launch {
//
//            viewModel.shelters.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED).collectLatest {
//                updateMapWithShelters(it)
//            }
//        }
//    }



    // 반경 내 대피소에 마커를 지도에 표시
    private fun updateMapWithShelters(shelters: List<EarthquakeOutdoorsShelterResponse.EarthquakeOutdoorsShelter2.Row>){
        // 기존 마커제거
        val marker = Marker()
        marker.map = null

        shelters.forEach{ shelter ->
            val latitude = shelter.ycord?.toDoubleOrNull()?.let { String.format("%.7f", it).toDouble() } ?: 0.0
            val longitude = shelter.xcord?.toDoubleOrNull()?.let { String.format("%.7f", it).toDouble() } ?: 0.0
            val shelterLocation = LatLng(latitude, longitude)

            marker.position = shelterLocation
            marker.map = naverMap
        }
    }


    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.locationSource = locationSource   //현재위치
        naverMap.uiSettings.isLocationButtonEnabled = true  //현재위치 버튼 활성화
        naverMap.locationTrackingMode = LocationTrackingMode.Follow //현재위치를 따라 움직임

        // 내위치 좌표를 받아와서 대피소 데이터 요청
        naverMap.addOnLocationChangeListener { location ->
            val currentLocation = LatLng(location.latitude, location.longitude)

            viewModel.getNearbyShelters(currentLocation, 1000.0,"arcd","ctprvnNm","sggNm")
        }
    }


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}