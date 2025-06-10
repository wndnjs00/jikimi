package com.myapp.jikimi.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapp.jikimi.data.model.dto.chatgpt.DisasterResponse
import com.myapp.jikimi.data.repository.Disaster.DisasterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DisasterViewModel @Inject constructor(
    private val repository: DisasterRepository
) : ViewModel() {

    private val _todayDisasters = MutableLiveData<List<DisasterResponse>>()
    val todayDisasters: LiveData<List<DisasterResponse>> = _todayDisasters

    private val _searchResult = MutableLiveData<DisasterResponse?>()
    val searchResult: LiveData<DisasterResponse?> = _searchResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        loadTodayDisasters()
    }

    fun loadTodayDisasters() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.getTodayDisasterTips()
                .onSuccess { disasters ->
                    _todayDisasters.value = disasters
                }
                .onFailure { exception ->
                    _errorMessage.value = exception.message
                }

            _isLoading.value = false
        }
    }

    fun searchDisaster(query: String) {
        if (query.isBlank()) {
            _searchResult.value = null
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.searchDisasterTips(query)
                .onSuccess { disaster ->
                    _searchResult.value = disaster
                }
                .onFailure { exception ->
                    _errorMessage.value = exception.message
                    _searchResult.value = null
                }

            _isLoading.value = false
        }
    }

    fun clearSearchResult() {
        _searchResult.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }
}