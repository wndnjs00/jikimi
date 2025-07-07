//package com.myapp.jikimi.presentation.state
//
//import com.myapp.jikimi.data.model.dto.EarthquakeIndoorsShelterResponse
//import com.myapp.jikimi.data.model.dto.EarthquakeOutdoorsShelterResponse
//import com.myapp.jikimi.data.model.entity.LikeEntity
//
//data class ShelterUiState(
//    val vtAcmdfcltyNm: String,
//    val classification: String,
//    val phone: String?,
//    val address: String,
//    val people: String?,
//    val distance: String,
//    val type: String,
//    val latitude: Double,
//    val longitude: Double
//) {
//    fun toLikeEntity(): LikeEntity {
//        return LikeEntity(
//            vtAcmdfcltyNm = vtAcmdfcltyNm,
//            rnAdres = address,
//            dtlAdres = "",
//            distanceData = distance,
//            shelterType = type,
//            latitude = latitude,
//            longitude = longitude
//        )
//    }
//
//    companion object {
//        fun fromOutdoor(data: EarthquakeOutdoorsShelterResponse.Shelter, distance: Double?): ShelterUiState {
//            val addr = data.eqkAcmdfcltyAdres ?: data.rnDtlAdres ?: data.dtlAdres ?: "데이터 없음"
//            return ShelterUiState(
//                vtAcmdfcltyNm = data.vtAcmdfcltyNm ?: "데이터 없음",
//                classification = data.vtAcmdfcltyNm ?: "데이터 없음",
//                phone = null,
//                address = addr,
//                people = data.vtAcmdPsblNmpr,
//                distance = String.format("%.2f", distance ?: 0.0),
//                type = "야외대피장소",
//                latitude = data.la?.toDoubleOrNull() ?: 0.0,
//                longitude = data.lo?.toDoubleOrNull() ?: 0.0
//            )
//        }
//
//        fun fromIndoor(data: EarthquakeIndoorsShelterResponse.EarthquakeIndoor.Row, distance: Double?): ShelterUiState {
//            val addr = data.rnAdres ?: data.dtlAdres ?: "데이터 없음"
//            return ShelterUiState(
//                vtAcmdfcltyNm = data.vtAcmdfcltyNm ?: "데이터 없음",
//                classification = data.acmdfcltyDtlCn ?: "데이터 없음",
//                phone = data.mngpsTelno,
//                address = addr,
//                people = data.vtAcmdPsblNmpr,
//                distance = String.format("%.2f", distance ?: 0.0),
//                type = "임시주거시설",
//                latitude = data.ycord?.toDoubleOrNull() ?: 0.0,
//                longitude = data.xcord?.toDoubleOrNull() ?: 0.0
//            )
//        }
//    }
//}
