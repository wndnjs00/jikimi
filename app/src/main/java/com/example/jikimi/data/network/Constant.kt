package com.example.jikimi.data.network

import com.example.jikimi.BuildConfig

// 행정안전부_지진 옥외대피장소
const val OUTDOOR_EVACUATION_API_BASE = BuildConfig.OUTDOOR_EVACUATION_API_BASE
const val OUTDOOR_EVACUATION_API = BuildConfig.OUTDOOR_EVACUATION_API
const val OUTDOOR_EVACUATION_SERVICE_KEY = BuildConfig.OUTDOOR_EVACUATION_SERVICE_KEY

// 행정안전부_지진겸용 임시주거시설
const val INDOOR_EVACUATION_API_BASE = BuildConfig.INDOOR_EVACUATION_API_BASE
const val INDOOR_EVACUATION_API = BuildConfig.INDOOR_EVACUATION_API
const val INDOOR_EVACUATION_SERVICE_KEY = BuildConfig.INDOOR_EVACUATION_SERVICE_KEY

// 국민행동요령_자연재난
const val NATURALDISASTER_API_BASE = BuildConfig.NATURALDISASTER_API_BASE
const val NATURALDISASTER_API = BuildConfig.NATURALDISASTER_API
const val NATURALDISASTER_SERVICE_KEY = BuildConfig.NATURALDISASTER_SERVICE_KEY


object Constant{
    // 공유되는 데이터
    const val OUTDOOR_SHELTER_DATA = "outdoor_shelter_data"
    const val OUTDOOR_DISTANCE_DATA = "outdoor_distance_data"
    const val INDOOR_SHELTER_DATA = "indoor_shelter_data"
    const val INDOOR_DISTANCE_DATA = "indoor_distance_data"
    const val ITEM_DATA = "item_data"
    const val ALL_ITEMS = "all_items"
}
