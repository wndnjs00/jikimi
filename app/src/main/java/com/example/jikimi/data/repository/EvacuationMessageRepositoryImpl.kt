package com.example.jikimi.data.repository

import com.example.jikimi.Resource
import com.example.jikimi.data.model.dto.EvacuationMessage
import com.example.jikimi.data.network.service.EvacuationMessageService
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class EvacuationMessageRepositoryImpl @Inject constructor(
    @Named("EvacuationMessageService") private val evacuationMessageService: EvacuationMessageService
) : EvacuationMessageRepository{

    override suspend fun getLatestEvacuationMessage(date: String): Resource<EvacuationMessage> {
        return try {
            // 이미 pageNo=1, numOfRows=1로 설정되어 있어 가장 최신 1개의 데이터만 요청하긴함
            val response = evacuationMessageService.getEvacuationMessage(crtDt = date)

            if (response.header.resultCode == "00" && response.body.isNotEmpty()) {
                // 가장 최근 메시지를 반환 (일반적으로 목록의 첫 번째 항목)
                Resource.Success(response.body[0])
            } else {
                Resource.Error("데이터가 없거나 API 응답이 올바르지 않습니다.")
            }
        } catch (e: Exception) {
            Resource.Error("재난 메시지를 가져오는 도중 오류가 발생했습니다: ${e.message}")
        }
    }
}