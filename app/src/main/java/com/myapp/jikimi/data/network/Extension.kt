package com.myapp.jikimi.data.network

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.myapp.jikimi.data.model.dto.chatgpt.DisasterResponse
import com.myapp.jikimi.data.model.entity.DisasterEntity
import com.naver.maps.geometry.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

// 하버사인 공식
fun LatLng.haversineDistance(other: LatLng): Double {
    val earthRadius = 6371e3 // 지구 반지름 (미터)
    val lat1 = this.latitude.toRadians()
    val lat2 = other.latitude.toRadians()
    val deltaLat = (other.latitude - this.latitude).toRadians()
    val deltaLon = (other.longitude - this.longitude).toRadians()

    val a = sin(deltaLat / 2).pow(2) +
            cos(lat1) * cos(lat2) * sin(deltaLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return earthRadius * c // 미터 단위로 반환
}

// Double의 확장 함수로 도 단위를 라디안으로 변환
fun Double.toRadians(): Double = Math.toRadians(this)


// dp 단위를 픽셀로 변환하는 유틸 함수
fun Int.dpToPx(context: Context): Int =
    (this * context.resources.displayMetrics.density).toInt()


fun DisasterResponse.toEntity(type: String): DisasterEntity {
    return DisasterEntity(
        id = 0, // auto-increment이므로 0으로 설정
        title = title,
        subtitle = subtitle,
        category = category,
        riskLevel = riskLevel,
        detailedSteps = Gson().toJson(detailedSteps),
        createdAt = System.currentTimeMillis(),
        type = type
    )
}

fun DisasterEntity.toDisasterResponse(): DisasterResponse {
    val steps = try {
        Gson().fromJson(detailedSteps, object : TypeToken<List<String>>() {}.type) ?: emptyList()
    } catch (e: Exception) {
        emptyList<String>()
    }

    return DisasterResponse(
        title = title,
        subtitle = subtitle,
        category = category,
        riskLevel = riskLevel,
        detailedSteps = steps
    )
}
