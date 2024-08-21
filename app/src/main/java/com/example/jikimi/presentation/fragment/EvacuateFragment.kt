package com.example.jikimi.presentation.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.jikimi.R
import com.example.jikimi.data.model.dto.Row
import com.example.jikimi.databinding.FragmentEvacuateBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EvacuateFragment : Fragment(), OnMapReadyCallback {
    private val binding get() = _binding!!
    private var _binding: FragmentEvacuateBinding? = null

    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap


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
        // 지도가 준비되었을때 onMapReady콜백 받도록
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


    // 내위치 좌표(위도,경도) 확인하기



    // 마커 표시
//    private fun setMapMarker(row: Row){
//        val latitude = row.ycord
//        val longitude = row.xcord
//        val latitude_formatter = String.format("%.7f", latitude).toDouble()
//        val longitude_formatter = String.format("%.7f", longitude).toDouble()
//    }



    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.locationSource = locationSource   //현재위치
        naverMap.uiSettings.isLocationButtonEnabled = true  //현재위치 버튼 활성화
        naverMap.locationTrackingMode = LocationTrackingMode.Follow //현재위치를 따라 움직임

        // 임시 마커 1개표시(마커잘되는지 확인위해)
        val marker = Marker()
        marker.position = LatLng(37.5670135, 126.9783740)
        marker.map = naverMap
    }


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}