package com.myapp.jikimi.data.repository.Auth

import android.net.Uri
import com.myapp.jikimi.Resource
import com.myapp.jikimi.data.model.dto.User

interface AuthRepository {
    suspend fun signup(email: String, password: String, nickname: String): Resource<User>
    suspend fun login(email: String, password: String): Resource<User>
    suspend fun logout(): Resource<Boolean>
    suspend fun deleteAccount(): Resource<Boolean>
    fun getCurrentUser(): User?
    fun isLoggedIn(): Boolean
    suspend fun getUserProfile(userId: String): Resource<User>
    suspend fun updateProfile(nickname: String, imageUri: Uri?): Resource<User>
}