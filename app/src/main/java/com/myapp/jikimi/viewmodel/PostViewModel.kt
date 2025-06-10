package com.myapp.jikimi.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapp.jikimi.Resource
import com.myapp.jikimi.data.model.dto.Post
import com.myapp.jikimi.data.repository.Post.PostRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _posts = MutableStateFlow<Resource<List<Post>>>(Resource.Loading())
    val posts: StateFlow<Resource<List<Post>>> = _posts.asStateFlow()

    private val _post = MutableStateFlow<Resource<Post>?>(null)
    val post: StateFlow<Resource<Post>?> = _post.asStateFlow()

    private val _createPostStatus = MutableStateFlow<Resource<Post>?>(null)
    val createPostStatus: StateFlow<Resource<Post>?> = _createPostStatus.asStateFlow()

    private val _updatePostStatus = MutableStateFlow<Resource<Post>?>(null)
    val updatePostStatus: StateFlow<Resource<Post>?> = _updatePostStatus.asStateFlow()

    private val _deletePostStatus = MutableStateFlow<Resource<Boolean>?>(null)
    val deletePostStatus: StateFlow<Resource<Boolean>?> = _deletePostStatus.asStateFlow()

    private val _userPosts = MutableStateFlow<Resource<List<Post>>>(Resource.Loading())
    val userPosts: StateFlow<Resource<List<Post>>> = _userPosts.asStateFlow()

    fun getPosts() {
        _posts.value = Resource.Loading()
        viewModelScope.launch {
            val result = postRepository.getAllPosts()
            _posts.value = result
        }
    }

    fun getPostById(postId: String) {
        _post.value = Resource.Loading()
        viewModelScope.launch {
            val result = postRepository.getPostById(postId)
            _post.value = result
        }
    }

    fun createPost(content: String, images: List<Uri>? = null, category: String = "소통") {
        _createPostStatus.value = Resource.Loading()
        viewModelScope.launch {
            val result = postRepository.createPost(content, images, category)
            _createPostStatus.value = result
        }
    }

    fun updatePost(
        postId: String,
        content: String,
        newImages: List<Uri>? = null,
        existingImages: List<String>? = null,
        category: String = "소통"
    ) {
        _updatePostStatus.value = Resource.Loading()
        viewModelScope.launch {
            val result = postRepository.updatePost(postId, content, newImages, existingImages, category)
            _updatePostStatus.value = result
        }
    }

    fun deletePost(postId: String) {
        _deletePostStatus.value = Resource.Loading()
        viewModelScope.launch {
            val result = postRepository.deletePost(postId)
            _deletePostStatus.value = result
        }
    }

    fun getPostsByUser(userId: String) {
        _userPosts.value = Resource.Loading()
        viewModelScope.launch {
            val result = postRepository.getPostsByUser(userId)
            _userPosts.value = result
        }
    }

    fun getBlockedPostIds(): Flow<List<String>> = flow {
        try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                val querySnapshot = FirebaseFirestore.getInstance().collection("blocked_posts")
                    .whereEqualTo("userId", currentUser.uid)
                    .get()
                    .await()

                val blockedIds = querySnapshot.documents.mapNotNull { doc ->
                    doc.getString("postId")
                }

                emit(blockedIds)
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            Log.e("PostViewModel", "차단된 게시물 ID 가져오기 실패: ${e.message}")
            emit(emptyList())
        }
    }

    // Reset methods for one-time events
    fun resetCreatePostStatus() {
        _createPostStatus.value = null
    }

    fun resetUpdatePostStatus() {
        _updatePostStatus.value = null
    }

    fun resetDeletePostStatus() {
        _deletePostStatus.value = null
    }
}