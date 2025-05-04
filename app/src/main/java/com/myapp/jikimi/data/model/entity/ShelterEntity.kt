package com.myapp.jikimi.data.model.entity

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
    val vtAcmdPsblNmpr: String,    //수용인원
    val acmdfcltyDtlCn: String,  // 시설명,
    val mngpsTelno: String,      // 전화번호,
    val lastUpdated: Long = System.currentTimeMillis(), // 마지막 업데이트 시간
)