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

    // 검색 결과가 없음을 나타내는 상태 추가
    private val _isSearchEmpty = MutableStateFlow(false)
    val isSearchEmpty: StateFlow<Boolean> = _isSearchEmpty.asStateFlow()

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
            _searchResult.value = null
            _isSearchEmpty.value = false
            return
        }

        viewModelScope.launch {
            _searchResult.value = Resource.Loading()
            _isSearchEmpty.value = false

            val result = repository.searchDisasterTips(query)

            if (result is Resource.Error && result.message == "재난과 관련된 키워드를 입력해주세요") {
                // 재난 관련 키워드가 아닌 경우
                _searchResult.value = null
                _isSearchEmpty.value = true
            } else {
                _searchResult.value = result
                _isSearchEmpty.value = false
            }
        }
    }

    fun clearSearchResult() {
        _searchResult.value = null
    }
}