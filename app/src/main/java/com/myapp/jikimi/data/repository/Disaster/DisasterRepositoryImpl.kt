package com.myapp.jikimi.data.repository.Disaster

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.myapp.jikimi.data.model.dto.chatgpt.ChatGPTRequest
import com.myapp.jikimi.data.model.dto.chatgpt.DisasterResponse
import com.myapp.jikimi.data.model.dto.chatgpt.Message
import com.myapp.jikimi.data.network.CHATGPT_API_SERVICE_KEY
import com.myapp.jikimi.data.network.service.ChatGPTApiService
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class DisasterRepositoryImpl @Inject constructor(
    @Named("ChatGPTService") private val apiService: ChatGPTApiService
) : DisasterRepository {

    private val gson = Gson()

    override suspend fun getTodayDisasterTips(): Result<List<DisasterResponse>> {
        return try {
            val prompt = createTodayDisasterPrompt()
            val request = ChatGPTRequest(
                messages = listOf(
                    Message("system", getSystemPrompt()),
                    Message("user", prompt)
                )
            )

            val response = apiService.getChatCompletion("Bearer $CHATGPT_API_SERVICE_KEY", request)
            if (response.isSuccessful) {
                val content = response.body()?.choices?.firstOrNull()?.message?.content
                content?.let { parseDisasterResponse(it, 3) }?.let { Result.success(it) }
                    ?: Result.failure(Exception("응답을 파싱할 수 없습니다"))
            } else {
                Result.failure(Exception("API 호출 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchDisasterTips(query: String): Result<DisasterResponse> {
        return try {
            val prompt = createSearchPrompt(query)
            val request = ChatGPTRequest(
                messages = listOf(
                    Message("system", getSystemPrompt()),
                    Message("user", prompt)
                )
            )

            val response = apiService.getChatCompletion("Bearer $CHATGPT_API_SERVICE_KEY", request)
            if (response.isSuccessful) {
                val content = response.body()?.choices?.firstOrNull()?.message?.content
                content?.let { parseDisasterResponse(it, 1) }?.firstOrNull()?.let { Result.success(it) }
                    ?: Result.failure(Exception("응답을 파싱할 수 없습니다"))
            } else {
                Result.failure(Exception("API 호출 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getSystemPrompt(): String {
        return """
        당신은 재난 안전 전문가입니다. 사용자의 요청에 따라 재난 대처 방법을 정확하고 실용적으로 제공해야 합니다.
        
        응답 형식:
        1. 반드시 JSON 형식으로만 응답하세요.
        2. 각 재난 대처법은 다음 구조를 따라야 합니다:

        {
          "disasters": [
            {
              "title": "재난 이름",
              "category": "자연재해/위험도",
              "riskLevel": "낮음/보통/높음",
              "detailedSteps": [
                "단계별 대처 방법 1",
                "단계별 대처 방법 2",
                "단계별 대처 방법 3",
                "단계별 대처 방법 4",
                "단계별 대처 방법 5"
              ],
//              "emergencyContact": "긴급연락처 정보"
            }
          ]
        }
        
        3. 모든 정보는 한국어로 제공하세요.
        4. 단계별 대처방법은 5개 이내로 제한하세요.
        5. 단계별 대처방법은 실제 상황에서 실용적으로 활용할 수 있는 내용으로 구성하세요.
        6. title은 "재난명" 빌셍시 대처방법 의 구조로 응답하세요.
        """.trimIndent()
    }

    private fun createTodayDisasterPrompt(): String {
        return """
        한국에서 발생할 수 있는 주요 재난 3가지에 대한 대처방법을 랜덤으로 제공해주세요.
        현재 계절과 날씨를 고려하여 자주 발생할 것 같은 재난으로 선정해주세요.
        각각 서로 다른 카테고리의 재난으로 구성해주세요.
        """.trimIndent()
    }

    private fun createSearchPrompt(query: String): String {
        return """
        다음 키워드와 관련된 재난 대처방법 1개를 제공해주세요: "$query"
        
        키워드와 가장 관련성이 높은 재난 상황을 선택하여 상세한 대처방법을 알려주세요.
        실제 상황에서 바로 활용할 수 있는 구체적이고 실용적인 내용으로 구성해주세요.
        """.trimIndent()
    }

    private fun parseDisasterResponse(content: String, expectedCount: Int): List<DisasterResponse>? {
        return try {
            val jsonContent = extractJsonFromContent(content)
            val responseData = gson.fromJson(jsonContent, DisasterApiResponse::class.java)
            responseData.disasters
        } catch (e: JsonSyntaxException) {
            null
        }
    }

    private fun extractJsonFromContent(content: String): String {
        val jsonStart = content.indexOf("{")
        val jsonEnd = content.lastIndexOf("}") + 1
        return if (jsonStart >= 0 && jsonEnd > jsonStart) {
            content.substring(jsonStart, jsonEnd)
        } else {
            content
        }
    }

    data class DisasterApiResponse(
        val disasters: List<DisasterResponse>
    )
}