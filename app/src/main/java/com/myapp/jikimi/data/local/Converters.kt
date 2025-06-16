//package com.myapp.jikimi.data.local
//
//import androidx.room.TypeConverter
//import com.google.gson.Gson
//import com.google.gson.reflect.TypeToken
//
//// List<String> 타입의 detailedSteps를 DB에 저장하려면 JSON 문자열로 변환이 필요
//class Converters {
//    @TypeConverter
//    fun fromStringList(value: List<String>): String {
//        return Gson().toJson(value)
//    }
//
//    @TypeConverter
//    fun toStringList(value: String): List<String> {
//        return try {
//            Gson().fromJson(value, object : TypeToken<List<String>>() {}.type)
//        } catch (e: Exception) {
//            emptyList()
//        }
//    }
//}