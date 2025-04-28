package com.example.jikimi.data.model.dto

import com.google.gson.annotations.SerializedName

data class EvacuationMessageResponse(
    @SerializedName("header") val header: Header,
    @SerializedName("numOfRows") val numOfRows: Int,
    @SerializedName("pageNo") val pageNo: Int,
    @SerializedName("totalCount") val totalCount: Int,
    @SerializedName("body") val body: List<EvacuationMessage>
)

data class Header(
    @SerializedName("resultMsg") val resultMsg: String,
    @SerializedName("resultCode") val resultCode: String,
    @SerializedName("errorMsg") val errorMsg: String?
)

data class EvacuationMessage(
    @SerializedName("MSG_CN") val messageContent: String,   // 메시지내용
    @SerializedName("RCPTN_RGN_NM") val regionName: String, // 수신지역명
    @SerializedName("CRT_DT") val createdDateTime: String,  //생성일시
    @SerializedName("REG_YMD") val registeredDate: String,  //등록일자
    @SerializedName("EMRG_STEP_NM") val emergencyStepName: String,  //긴급단계명
    @SerializedName("SN") val serialNumber: Int,                //일련번호
    @SerializedName("DST_SE_NM") val disasterTypeName: String,  //재해구분명
    @SerializedName("MDFCN_YMD") val modifiedDate: String   //수정일자
)