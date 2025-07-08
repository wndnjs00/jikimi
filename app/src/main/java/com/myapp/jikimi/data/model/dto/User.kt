package com.myapp.jikimi.data.model.dto

data class User(
    val uid: String = "",
    val email: String = "",
    val nickname: String = "",
    val profileImageUrl: String = "", // 프로필 이미지 URL
    val createdAt: Long = System.currentTimeMillis()
)