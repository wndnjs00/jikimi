package com.example.jikimi.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// 검색 기능을 위한 Entitiy
@Entity
data class ShelterEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vtAcmdfcltyNm: String,  // 대피시설명
    val address: String?,       // 주소
    val detailAddress: String?,      // 상세주소
    val latitude: Double,       // 위도
    val longitude: Double,      // 경도
    val shelterType: String,    // "INDOOR" or "OUTDOOR"
)