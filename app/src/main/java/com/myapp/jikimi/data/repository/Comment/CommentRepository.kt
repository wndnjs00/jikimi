package com.myapp.jikimi.data.repository.Comment

import com.myapp.jikimi.Resource
import com.myapp.jikimi.data.model.dto.Comment

interface CommentRepository {
    suspend fun getCommentsByPost(postId: String): Resource<List<Comment>>
    suspend fun addComment(
        postId: String,
        content: String,
        parentCommentId: String = ""
    ): Resource<Comment>

    suspend fun deleteComment(commentId: String, postId: String): Resource<Boolean>
}