package com.example.jikimi.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.jikimi.data.model.dto.Item
import com.example.jikimi.data.model.dto.SocialItem
import com.example.jikimi.data.repository.SocialDisasterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// 사회재난 눌렀을때 받아올 데이터
@HiltViewModel
class SocialDisasterViewModel @Inject constructor(
    private val socialDisasterRepository: SocialDisasterRepository
) : ViewModel(){

    private val _socialDisaster = MutableStateFlow<SocialItem?>(null)
    val socialDisaster: StateFlow<SocialItem?> = _socialDisaster

    private val _socialClickItem = MutableStateFlow<List<SocialItem>>(emptyList())
    val socialClickItem: StateFlow<List<SocialItem>> = _socialClickItem


    fun getSocialDisaster(safetyCates: List<String>){
        viewModelScope.launch{
            safetyCates.forEach{ safetyCate ->
                try{
                    // API 호출
                    val socialResponse = socialDisasterRepository.requestSocialDisaster(safetyCate)

                    if (socialResponse != null) {
                        val socialItems = socialResponse.body?.items?.item ?: emptyList()

                        // contentsType이 1인 데이터 필터링 후 첫 번째 데이터만 가져옴
                        val filteredSocialItem = socialItems.firstOrNull {
                            it.contentsType == "1"
                        }

                        _socialDisaster.value = filteredSocialItem
                        Log.d("socialDisaster API호출 성공", "데이터 : ${socialResponse}")
                    }else{
                        Log.e("socialDisaster_null_error", "response is null")
                    }
                } catch (e: Exception){
                    Log.d("socialDisaster API호출 실패", "API 받아오기 실패: ${e.message}")
                }
            }
        }
    }


    fun getClickSocialItem(safetyCates: String){
        viewModelScope.launch {
            try{
                // API 호출
                val socialResponse = socialDisasterRepository.requestSocialDisaster(safetyCates)

                if (socialResponse != null) {
                    val socialItems = socialResponse.body?.items?.item ?: emptyList()
                    _socialClickItem.value = socialItems
                }else{
                    Log.e("socialDisaster_null_error", "response is null")
                }
        } catch(e: Exception){
                Log.d("socialDisaster API 호출 실패", "API 받아오기 실패: ${e.message}")
            }
        }
    }
}