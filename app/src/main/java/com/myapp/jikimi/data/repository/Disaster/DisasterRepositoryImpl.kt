package com.myapp.jikimi.data.repository.Disaster

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.myapp.jikimi.Resource
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

    override suspend fun getTodayDisasterTips(): Resource<List<DisasterResponse>> {
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
                content?.let { parseDisasterResponse(it, 3) }?.let {
                    Resource.Success(it)
                } ?: Resource.Error("응답을 파싱할 수 없습니다")
            } else {
                Resource.Error("API 호출 실패: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "알 수 없는 오류가 발생했습니다")
        }
    }

    override suspend fun searchDisasterTips(query: String): Resource<DisasterResponse> {
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
                content?.let { parseDisasterResponse(it, 1) }?.firstOrNull()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("응답을 파싱할 수 없습니다")
            } else {
                Resource.Error("API 호출 실패: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "알 수 없는 오류가 발생했습니다")
        }
    }

    private fun getSystemPrompt(): String {
        return """
        당신은 재난 안전 전문가입니다.
        한국정부의 재난 대피 매뉴얼을 기준으로, 해당 재난발생시 즉시 취해야할 행동가이드를 JSON 형식으로 제공합니다.
        
        응답 형식:
        1. 반드시 JSON 형식으로만 응답하세요.
        2. 각 재난 대처법은 다음 구조를 따라야 합니다:

        {
          "disasters": [
            {
              "title": "재난 이름",
              "subtitle": "재난상황과 관련한 한줄 설명",
              "category": "자연재해/위험도",
              "riskLevel": "낮음/보통/높음",
              "detailedSteps": [
                "단계별 대처 방법 1",
                "단계별 대처 방법 2",
                "단계별 대처 방법 3",
                "단계별 대처 방법 4",
                "단계별 대처 방법 5"
              ],
            }
          ]
        }
        
        3. 모든 정보는 한국어로 제공하세요.
        4. 단계별 대처방법은 5개 이내로 제한하세요.
        5. 단계별 대처방법은 실제 상황에서 실질적으로 대처할 수 있는 내용으로, 30글자 이내로 구성하세요.
        6. 단계별 대처방법은 최대한 구체적이고 정확한 정보로 제공하세요. (사람이 할수없는 '순간이동'같은 행동은 제공하지마세요.)
        7. title은 "'재난명' 대처방법" 의 구조로 응답하세요.
        8. 대처방법은 "합니다" 로 끝나도록하세요.
        9. 위험도는 사망률이 20%이하면 낮음, 40%이하면 보통, 90%이하면 높음으로 구분하세요.
        10. subtitle은 20글자 이내로 표시하고, 반드시 명사로 끝나도록하세요.
        11. subtitle 구성방식에 대한 예시: 재난상황 = "지진", subtitle ="갑작스런 지진 상황에서의 생존가이드"
        """.trimIndent()
    }

    private fun createTodayDisasterPrompt(): String {
        return """
        한국에서 발생할 수 있는 주요 재난 3가지에 대한 대처방법을 랜덤으로 제공해주세요.
        각각 서로 다른 카테고리의 재난으로 구성해주세요.
        """.trimIndent()
    }

    private fun createSearchPrompt(query: String): String {
        return """
        다음 키워드와 관련된 재난 대처방법 1개를 제공해주세요: "$query"
        getSystemPrompt()에서 명령한 JSON형식과 똑같이 제공해주세요.
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
