package com.myapp.jikimi.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "disaster_tips")
data class DisasterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subtitle: String,
    val category: String,
    val riskLevel: String,
    val detailedSteps: String, // JSON 형태로 저장
    val createdAt: Long,
    val type: String // "today" 또는 "search"
)