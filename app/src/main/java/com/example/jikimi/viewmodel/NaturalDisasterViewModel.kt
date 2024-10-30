package com.example.jikimi.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jikimi.data.model.dto.Item
import com.example.jikimi.data.repository.NaturalDisasterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// 자연재난 눌렀을때 받아올 데이터
@HiltViewModel
class NaturalDisasterViewModel @Inject constructor(
    private val naturalDisasterRepository: NaturalDisasterRepository
) : ViewModel() {

    private val _naturalDisaster = MutableStateFlow<Item?>(null)
    val naturalDisaster: StateFlow<Item?> = _naturalDisaster

    private val _clickItem = MutableStateFlow<List<Item>>(emptyList())
    val clickItem: StateFlow<List<Item>> = _clickItem


    fun getNaturalDisaster(safetyCates: List<String>) {
        viewModelScope.launch {
            safetyCates.forEach { safetyCate ->
                try {
                    // API호출
                    val response = naturalDisasterRepository.requestNaturalDisaster(safetyCate)

                    if (response != null) {
                        val items = response.body?.items?.item ?: emptyList()

                        // contentsType이 1인 데이터 필터링 후 첫 번째 데이터만 가져옴
                        val filteredItem = items.firstOrNull {
                            it.contentsType == "1"
                        }

                        _naturalDisaster.value = filteredItem

                        Log.d("naturalDisaster API호출 성공", "데이터 : ${response}")
                    } else {
                        Log.e("naturalDisaster_null_error", "response is null")
                    }
                } catch (e: Exception) {
                    Log.d("naturalDisaster API호출 실패", "API 받아오기 실패: ${e.message}")
                }
            }
        }
    }


    // 자연재난 아이템 클릭했을때
    fun getClickItem(safetyCates: String) {
        viewModelScope.launch {
            try {
                val responses = naturalDisasterRepository.requestNaturalDisaster(safetyCates)

                if (responses != null) {
                    val items = responses.body?.items?.item ?: emptyList()
                    _clickItem.value = items
                } else {
                    Log.e("naturalDisaster_null_error", "response is null")
                }
            } catch (e: Exception) {
                Log.d("naturalDisaster API 호출 실패", "API 받아오기 실패: ${e.message}")
            }
        }
    }
}
