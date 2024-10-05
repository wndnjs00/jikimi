package com.example.jikimi.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jikimi.data.model.dto.Item
import com.example.jikimi.data.repository.NaturalDisasterRepository
import com.example.jikimi.presentation.adapter.CommonsenseAdapter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// 자연재난 눌렀을때 받아올 데이터
@HiltViewModel
class CommonsenseViewModel @Inject constructor(
    private val naturalDisasterRepository: NaturalDisasterRepository
) : ViewModel(){

    private val _naturalDisaster = MutableStateFlow<List<Item>>(emptyList())
    val naturalDisaster : StateFlow<List<Item>> = _naturalDisaster

    fun getNaturalDisaster(safetyCate : String){
        viewModelScope.launch{
            try {
                // API호출
                val response = naturalDisasterRepository.requestNaturalDisaster(safetyCate)
                _naturalDisaster.value = response.body.items.item
                Log.d("naturalDisaster API호출 성공", "데이터 : ${response}")

            } catch (e: Exception){
                Log.d("naturalDisaster API호출 실패", "API 받아오기 실패: ${e.message}")
            }

        }
    }
}