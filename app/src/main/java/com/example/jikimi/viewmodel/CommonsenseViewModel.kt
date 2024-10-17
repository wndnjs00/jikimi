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

    fun getNaturalDisaster(safetyCates : List<String>){
        viewModelScope.launch{
            val allItems = mutableListOf<Item>()
            safetyCates.forEach{ safetyCate ->
                try {
                    // API호출
                    val response = naturalDisasterRepository.requestNaturalDisaster(safetyCate)

                    if(response != null){
                        val items = response.body?.items?.item ?: emptyList()
                        allItems.addAll(items)  // 결과를 리스트에 추가
                        Log.d("naturalDisaster API호출 성공", "데이터 : ${response}")
                    }else{
                        Log.e("naturalDisaster_null_error", "response is null")
                    }
                } catch (e: Exception){
                    Log.d("naturalDisaster API호출 실패", "API 받아오기 실패: ${e.message}")
                }
            }
            // 모든 결과를 한번에 업데이트하는데, 중복되는 값은 필터링(safetyCateNm2를 기준으로)
//            val filterItems = allItems.distinctBy { it.safetyCateNm2 }
            _naturalDisaster.value = allItems
        }
    }
}