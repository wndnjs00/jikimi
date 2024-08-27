package com.example.jikimi.viewmodel

import androidx.lifecycle.ViewModel
import com.example.jikimi.data.repository.OutdoorEvacuationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OutdoorEvacuationViewModel @Inject constructor(
    private val outdoorEvacuationRepository: OutdoorEvacuationRepository,

) : ViewModel(){

}