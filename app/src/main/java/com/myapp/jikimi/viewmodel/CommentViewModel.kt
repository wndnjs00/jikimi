package com.myapp.jikimi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapp.jikimi.Resource
import com.myapp.jikimi.data.model.dto.Comment
import com.myapp.jikimi.data.repository.Comment.CommentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val commentRepository: CommentRepository
) : ViewModel() {

    private val _comments = MutableStateFlow<Resource<List<Comment>>>(Resource.Loading())
    val comments: StateFlow<Resource<List<Comment>>> = _comments.asStateFlow()

    private val _addCommentStatus = MutableStateFlow<Resource<Comment>?>(null)
    val addCommentStatus: StateFlow<Resource<Comment>?> = _addCommentStatus.asStateFlow()

    private val _deleteCommentStatus = MutableStateFlow<Resource<Boolean>?>(null)
    val deleteCommentStatus: StateFlow<Resource<Boolean>?> = _deleteCommentStatus.asStateFlow()

    fun getCommentsByPost(postId: String) {
        _comments.value = Resource.Loading()
        viewModelScope.launch {
            val result = commentRepository.getCommentsByPost(postId)
            _comments.value = result
        }
    }

    fun addComment(postId: String, content: String, parentCommentId: String = "") {
        _addCommentStatus.value = Resource.Loading()
        viewModelScope.launch {
            val result = commentRepository.addComment(postId, content, parentCommentId)
            _addCommentStatus.value = result
        }
    }

    fun deleteComment(commentId: String, postId: String) {
        _deleteCommentStatus.value = Resource.Loading()
        viewModelScope.launch {
            val result = commentRepository.deleteComment(commentId, postId)
            _deleteCommentStatus.value = result
        }
    }

    // Reset status after handling events
    fun resetAddCommentStatus() {
        _addCommentStatus.value = null
    }

    fun resetDeleteCommentStatus() {
        _deleteCommentStatus.value = null
    }
}