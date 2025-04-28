package com.example.jikimi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jikimi.Resource
import com.example.jikimi.data.model.dto.EvacuationMessage
import com.example.jikimi.data.repository.EvacuationMessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class EvacuationMessageViewModel @Inject constructor(
    private val evacuationMessageRepository: EvacuationMessageRepository
) : ViewModel() {

    private val _evacuationMessage = MutableStateFlow<Resource<EvacuationMessage>>(Resource.Loading())
    val evacuationMessage: StateFlow<Resource<EvacuationMessage>> = _evacuationMessage

    init {
        getLatestEvacuationMessage()
    }

    fun getLatestEvacuationMessage() {
        viewModelScope.launch {
            _evacuationMessage.value = Resource.Loading()

            // 오늘 날짜를 yyyyMMdd 형식으로 포맷팅
            val todayDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

            // API 호출 및 결과 처리
            val result = evacuationMessageRepository.getLatestEvacuationMessage(todayDate)
            _evacuationMessage.value = result
        }
    }

    // 새로고침을 위한 메서드
    fun refreshEvacuationMessage() {
        getLatestEvacuationMessage()
    }
}