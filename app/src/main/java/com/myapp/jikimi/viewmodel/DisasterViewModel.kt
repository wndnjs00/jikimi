package com.myapp.jikimi.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapp.jikimi.Resource
import com.myapp.jikimi.data.model.dto.chatgpt.DisasterResponse
import com.myapp.jikimi.data.repository.Disaster.DisasterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DisasterViewModel @Inject constructor(
    private val repository: DisasterRepository
) : ViewModel() {

    private val _todayDisasters = MutableStateFlow<Resource<List<DisasterResponse>>>(Resource.Loading())
    val todayDisasters: StateFlow<Resource<List<DisasterResponse>>> = _todayDisasters.asStateFlow()

    private val _searchResult = MutableStateFlow<Resource<DisasterResponse>?>(null)
    val searchResult: StateFlow<Resource<DisasterResponse>?> = _searchResult.asStateFlow()

    init {
        loadTodayDisasters()
    }

    fun loadTodayDisasters() {
        viewModelScope.launch {
            _todayDisasters.value = Resource.Loading()

            val result = repository.getTodayDisasterTips()
            _todayDisasters.value = result
        }
    }

    fun searchDisaster(query: String) {
        if (query.isBlank()) {
            _searchResult.value = null // 초기 상태로 리셋
            return
        }

        viewModelScope.launch {
            _searchResult.value = Resource.Loading()

            val result = repository.searchDisasterTips(query)
            _searchResult.value = result
        }
    }

    fun clearSearchResult() {
        _searchResult.value = null
    }
}