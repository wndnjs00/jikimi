package com.example.jikimi.data.repository

import com.example.jikimi.data.model.dto.NaturalDisasterResponse
import com.example.jikimi.data.network.service.NaturalDisasterService
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class NaturalDisasterRepositoryImpl @Inject constructor(
    @Named("NaturalDisasterService") private val naturalDisasterService: NaturalDisasterService
) : NaturalDisasterRepository{

    override suspend fun requestNaturalDisaster(safetyCate : String): NaturalDisasterResponse {
        return naturalDisasterService.getNaturalDisaster(safetyCate = safetyCate)
    }
}