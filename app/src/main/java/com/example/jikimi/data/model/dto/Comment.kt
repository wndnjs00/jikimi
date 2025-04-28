package com.example.jikimi.data.model.dto

data class Comment(
    val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val nickname: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val profileImageUrl: String = "", // 프로필 이미지 URL 추가
    val parentCommentId: String = "", // 부모 댓글 ID (일반 댓글은 빈 문자열, 답글은 부모 댓글 ID)
    val isReply: Boolean = false // 답글 여부
)