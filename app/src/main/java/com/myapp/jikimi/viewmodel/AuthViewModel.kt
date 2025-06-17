package com.myapp.jikimi.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapp.jikimi.Resource
import com.myapp.jikimi.data.model.dto.User
import com.myapp.jikimi.data.repository.Auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _signupStatus = MutableStateFlow<Resource<User>>(Resource.Loading(null))
    val signupStatus = _signupStatus.asStateFlow()

    private val _loginStatus = MutableStateFlow<Resource<User>>(Resource.Loading(null))
    val loginStatus = _loginStatus.asStateFlow()

    private val _logoutStatus = MutableStateFlow<Resource<Boolean>>(Resource.Loading(null))
    val logoutStatus = _logoutStatus.asStateFlow()

    private val _userProfile = MutableStateFlow<Resource<User>>(Resource.Loading(null))
    val userProfile = _userProfile.asStateFlow()

    private val _updateProfileStatus = MutableStateFlow<Resource<User>>(Resource.Loading(null))
    val updateProfileStatus = _updateProfileStatus.asStateFlow()

    private val _deleteAccountStatus = MutableStateFlow<Resource<Boolean>>(Resource.Loading(null))
    val deleteAccountStatus = _deleteAccountStatus.asStateFlow()


    // 회원가입
    fun signup(email: String, password: String, nickname: String) {
        _signupStatus.value = Resource.Loading()
        viewModelScope.launch {
            val result = authRepository.signup(email, password, nickname)
            _signupStatus.value = result
        }
    }

    // 로그인
    fun login(email: String, password: String) {
        _loginStatus.value = Resource.Loading()
        viewModelScope.launch {
            val result = authRepository.login(email, password)
            _loginStatus.value = result
        }
    }

    // 로그아웃
    fun logout() {
        _logoutStatus.value = Resource.Loading()
        viewModelScope.launch {
            val result = authRepository.logout()
            _logoutStatus.value = result
        }
    }

    // 회원탈퇴
    fun deleteAccount() {
        _deleteAccountStatus.value = Resource.Loading()
        viewModelScope.launch {
            val result = authRepository.deleteAccount()
            _deleteAccountStatus.value = result
        }
    }

    fun getCurrentUser() = authRepository.getCurrentUser()


    fun getUserProfile(userId: String) {
        _userProfile.value = Resource.Loading()
        viewModelScope.launch {
            val result = authRepository.getUserProfile(userId)
            _userProfile.value = result
        }
    }

    fun updateProfile(nickname: String, imageUri: Uri?) {
        _updateProfileStatus.value = Resource.Loading()
        viewModelScope.launch {
            Log.d("AuthViewModel", "프로필 업데이트 시작 - 닉네임: $nickname, 이미지 URI: $imageUri")
            val result = authRepository.updateProfile(nickname, imageUri)
            _updateProfileStatus.value = result
        }
    }

    fun isLoggedIn() = authRepository.isLoggedIn()
}