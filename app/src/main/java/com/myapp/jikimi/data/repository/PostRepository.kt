package com.myapp.jikimi.data.repository

import android.net.Uri
import com.myapp.jikimi.Resource
import com.myapp.jikimi.data.model.dto.Post

interface PostRepository {
    suspend fun getAllPosts(): Resource<List<Post>>
    suspend fun getPostById(postId: String): Resource<Post>
    suspend fun createPost(content: String, images: List<Uri>? = null, category: String = "소통"): Resource<Post>
    suspend fun updatePost(postId: String, content: String, newImages: List<Uri>? = null, existingImages: List<String>? = null, category: String = "소통"): Resource<Post>
    suspend fun deletePost(postId: String): Resource<Boolean>
    suspend fun getPostsByUser(userId: String): Resource<List<Post>>
}