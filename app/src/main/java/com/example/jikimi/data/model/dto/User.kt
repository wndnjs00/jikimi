package com.example.jikimi.data.model.dto

data class User(
    val uid: String = "",
    val email: String = "",
    val nickname: String = "",
    val profileImageUrl: String = "", // 프로필 이미지 URL 추가
    val createdAt: Long = System.currentTimeMillis()
)