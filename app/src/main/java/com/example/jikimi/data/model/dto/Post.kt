package com.example.jikimi.data.model.dto

data class Post(
    val id: String = "",
    val userId: String = "",
    val nickname: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val commentCount: Int = 0,
    val profileImageUrl: String = "", // 프로필 이미지 URL 추가
    val imageUrls: List<String> = emptyList(), // 이미지 URL 목록 추가
    val category: String = ""   // 스피너 카테고리
)