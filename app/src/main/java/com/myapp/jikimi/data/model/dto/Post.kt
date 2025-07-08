package com.myapp.jikimi.data.model.dto

data class Post(
    val id: String = "",
    val userId: String = "",
    val nickname: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val commentCount: Int = 0,
    val profileImageUrl: String = "", // 프로필 이미지 URL
    val imageUrls: List<String> = emptyList(), // 이미지 URL 목록
    val category: String = ""   // 스피너 카테고리
)