package com.myapp.jikimi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapp.jikimi.data.model.entity.LikeEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor() : ViewModel() {
    // LikeBottomSheetFragment와 EvacuateFragment사이에서 데이터 공유하기위해
    private val _selectedLikeEntity = MutableStateFlow<LikeEntity?>(null)
    val selectedLikeEntity: StateFlow<LikeEntity?> = _selectedLikeEntity.asStateFlow()

    // userNickname데이터를 evacuateFragment와 commensenseFragment에서 공유하기 위해
    private val _userNickname = MutableStateFlow<String?>(null)
    val userNickname: StateFlow<String?> = _userNickname.asStateFlow()

    fun selectLikeEntity(likeEntity: LikeEntity) {
        _selectedLikeEntity.value = likeEntity
    }

    fun updateNickname(nickname: String) {
        viewModelScope.launch {
            _userNickname.value = nickname
        }
    }
}