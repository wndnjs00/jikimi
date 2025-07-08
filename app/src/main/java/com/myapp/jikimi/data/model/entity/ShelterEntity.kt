package com.myapp.jikimi.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ShelterEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val shelterName: String,  // 대피시설명
    val address: String?,       // 주소
    val detailAddress: String?, // 상세주소
    val latitude: Double,       // 위도
    val longitude: Double,      // 경도
    val shelterType: String,    // "INDOOR" or "OUTDOOR"
    val capacityNumber: String,    //수용인원
    val shelterDetailName: String,  // 시설명,
    val phoneNumber: String,      // 전화번호,
    val lastUpdated: Long = System.currentTimeMillis(), // 마지막 업데이트 시간
)