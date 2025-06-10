package com.myapp.jikimi.data.network
import com.myapp.jikimi.BuildConfig

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


// 챗지피티
const val CHATGPT_API_BASE = BuildConfig.CHATGPT_API_BASE
const val CHATGPT_API = BuildConfig.CHATGPT_API
const val CHATGPT_API_SERVICE_KEY = BuildConfig.CHATGPT_API_SERVICE_KEY



object Constant{
    // 공유되는 데이터
    const val OUTDOOR_SHELTER_DATA = "outdoor_shelter_data"
    const val OUTDOOR_DISTANCE_DATA = "outdoor_distance_data"
    const val INDOOR_SHELTER_DATA = "indoor_shelter_data"
    const val INDOOR_DISTANCE_DATA = "indoor_distance_data"
}

// SharedPreferences에서 사용할 키 상수
const val PREF_NAME = "EarthquakeShelterPrefs"
const val KEY_LAST_INDOOR_UPDATE = "last_indoor_update"     // 마지막 업데이트 시간
const val KEY_LAST_OUTDOOR_UPDATE = "last_outdoor_update"   // 마지막 업데이트 시간
const val KEY_TOTAL_INDOOR_COUNT = "total_indoor_count"     // API를 통해 가져온 대피소 데이터의 총 개수
const val KEY_TOTAL_OUTDOOR_COUNT = "total_outdoor_count"   // API를 통해 가져온 대피소 데이터의 총 개수
const val UPDATE_INTERVAL = 90 * 24 * 60 * 60 * 1000L // 90일