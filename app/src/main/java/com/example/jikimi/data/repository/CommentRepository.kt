package com.example.jikimi.data.repository

import com.example.jikimi.Resource
import com.example.jikimi.data.model.dto.Comment

interface CommentRepository {
    suspend fun getCommentsByPost(postId: String): Resource<List<Comment>>
    suspend fun addComment(postId: String, content: String, parentCommentId: String = ""): Resource<Comment>
    suspend fun deleteComment(commentId: String, postId: String): Resource<Boolean>
}