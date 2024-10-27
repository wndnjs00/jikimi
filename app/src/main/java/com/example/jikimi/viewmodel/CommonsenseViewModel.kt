package com.example.jikimi.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jikimi.data.model.dto.Item
import com.example.jikimi.data.model.dto.Items
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

                        // contentsType이 2 또는 3인 데이터 필터링 후 첫 번째 데이터만 가져옴 / 해당 데이터 없을시 contentsType가 1인 첫번째값 호출
                        val filteredItem = items.firstOrNull {
                            it.contentsType == "2" || it.contentsType == "3"
                        } ?: items.firstOrNull {
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
