package com.example.jikimi.data.repository

import android.net.Uri
import com.example.jikimi.Resource
import com.example.jikimi.data.model.dto.User

interface AuthRepository {
    suspend fun signup(email: String, password: String, nickname: String): Resource<User>
    suspend fun login(email: String, password: String): Resource<User>
    suspend fun logout(): Resource<Boolean>
    fun getCurrentUser(): User?
    fun isLoggedIn(): Boolean
    suspend fun getUserProfile(userId: String): Resource<User>
    suspend fun updateProfile(nickname: String, imageUri: Uri?): Resource<User>
}