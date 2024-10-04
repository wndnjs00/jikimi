package com.example.jikimi.viewmodel

import androidx.lifecycle.ViewModel
import com.example.jikimi.data.model.entity.LikeEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class LikeSharedViewModel @Inject constructor() : ViewModel(){
    // LikeBottomSheetFragment와 EvacuateFragment사이에서 데이터 공유하기위해
    private val _selectedLikeEntity = MutableStateFlow<LikeEntity?>(null)
    val selectedLikeEntity : StateFlow<LikeEntity?> = _selectedLikeEntity.asStateFlow()

    fun selectLikeEntity(likeEntity: LikeEntity){
        _selectedLikeEntity.value = likeEntity
    }
}