package com.example.jikimi.data.network

import com.example.jikimi.BuildConfig

// 행정안전부_지진 옥외대피장소
const val OUTDOOR_EVACUATION_API_BASE = BuildConfig.OUTDOOR_EVACUATION_API_BASE
const val OUTDOOR_EVACUATION_API = BuildConfig.OUTDOOR_EVACUATION_API
const val OUTDOOR_EVACUATION_SERVICE_KEY = BuildConfig.OUTDOOR_EVACUATION_SERVICE_KEY

// 행정안전부_지진겸용 임시주거시설(변경완료)
const val INDOOR_EVACUATION_API_BASE = BuildConfig.INDOOR_EVACUATION_API_BASE
const val INDOOR_EVACUATION_API = BuildConfig.INDOOR_EVACUATION_API
const val INDOOR_EVACUATION_SERVICE_KEY = BuildConfig.INDOOR_EVACUATION_SERVICE_KEY

// 제난 안전문자
const val EVACUATION_MESSAGE_API_BASE = BuildConfig.EVACUATION_MESSAGE_API_BASE
const val EVACUATION_MESSAGE_API = BuildConfig.EVACUATION_MESSAGE_API
const val EVACUATION_MESSAGE_SERVICE_KEY = BuildConfig.EVACUATION_MESSAGE_SERVICE_KEY



object Constant{
    // 공유되는 데이터
    const val OUTDOOR_SHELTER_DATA = "outdoor_shelter_data"
    const val OUTDOOR_DISTANCE_DATA = "outdoor_distance_data"
    const val INDOOR_SHELTER_DATA = "indoor_shelter_data"
    const val INDOOR_DISTANCE_DATA = "indoor_distance_data"
}
