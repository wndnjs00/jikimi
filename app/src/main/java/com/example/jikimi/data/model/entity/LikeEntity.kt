package com.example.jikimi.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LikeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vtAcmdfcltyNm : String,
    val rnAdres : String,
    val vtAcmdPsblNmpr : String,
    val dtlAdres : String,
    val distanceData : String,
    val shelterType : String
)