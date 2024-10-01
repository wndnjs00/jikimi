package com.example.jikimi.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jikimi.data.local.dao.ShelterDao
import com.example.jikimi.data.model.entity.LikeEntity
import com.example.jikimi.presentation.VisibilityView
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LikeViewModel @Inject constructor(
    private val shelterDao: ShelterDao
) : ViewModel(){

    // Room으로 모든 데이터 가져옴
    private val _likeEntity = MutableStateFlow<List<LikeEntity>>(emptyList())
    val likeEntity : StateFlow<List<LikeEntity>> = _likeEntity.asStateFlow()

    init {
        viewModelScope.launch {
            shelterDao.getAllData().collect{
                _likeEntity.value = it
                visibilityView()
            }
        }
    }

    fun saveData(likeEntity: LikeEntity) = viewModelScope.launch {
        withContext(Dispatchers.IO){
            shelterDao.insertData(likeEntity)
        }
    }

    // 데이터유무에 따라 보이는화면 설정하기위해
    private val _visibilityView = MutableStateFlow<VisibilityView>(VisibilityView.EMPTYVIEW)
    val visibilityView: StateFlow<VisibilityView> = _visibilityView.asStateFlow()

    fun visibilityView(){
        if(likeEntity.value.isEmpty()){
            _visibilityView.value = VisibilityView.EMPTYVIEW
        }else{
            _visibilityView.value = VisibilityView.RECYCLERVIEW
        }
    }

    // 데이터삭제
    fun deleteData(likeEntity: LikeEntity) = viewModelScope.launch {
        withContext(Dispatchers.IO){
            shelterDao.deleteData(likeEntity)

            // 삭제 후 데이터가 제대로 반영되었는지 로그로 확인
            val updatedEntities = shelterDao.getAllData().first()
            Log.d("LikeViewModelss", "삭제 후 업데이트된 데이터: $updatedEntities")
        }
    }
}
