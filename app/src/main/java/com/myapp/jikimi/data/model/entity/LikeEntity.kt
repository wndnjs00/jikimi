package com.myapp.jikimi.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LikeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val shelterName: String, // 대피시설명
    val roadAddress: String,    // 도로명주소
    val detailAddress: String, //상세주소
    val distanceData: String,
    val shelterType: String,
    val latitude: Double,
    val longitude: Double,
)