package com.example.jikimi.data.network

import android.location.Location
import com.naver.maps.geometry.LatLng

// 두지점간의 거리 계산 확장함수
fun LatLng.distanceExtention(other: LatLng): Double {
    val results = FloatArray(1)
    Location.distanceBetween(this.latitude, this.longitude, other.latitude, other.longitude, results)
    return results[0].toDouble()
}
