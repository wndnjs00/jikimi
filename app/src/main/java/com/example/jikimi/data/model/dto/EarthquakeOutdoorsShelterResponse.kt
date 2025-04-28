package com.example.jikimi.data.model.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class EarthquakeOutdoorsShelterResponse(
    @SerializedName("header") val header: Header,
    @SerializedName("numOfRows") val numOfRows: Int,
    @SerializedName("pageNo") val pageNo: Int,
    @SerializedName("totalCount") val totalCount: Int,
    @SerializedName("body") val body: List<Shelter>
) {
    data class Header(
        @SerializedName("resultMsg") val resultMsg: String?,
        @SerializedName("resultCode") val resultCode: String?,
        @SerializedName("errorMsg") val errorMsg: String?
    )

    @Parcelize
    data class Shelter(
        @SerializedName("DTL_ADRES") val dtlAdres: String?, //상세주소
        @SerializedName("USE_SE_CD") val useSeCd: String?,  //사용구분코드
        @SerializedName("LO") val lo: String?,  //경도
        @SerializedName("VT_ACMDFCLTY_NM") val vtAcmdfcltyNm: String?,//이재민수용시설명
        @SerializedName("RN_DTL_ADRES") val rnDtlAdres: String?,    //도로명상세주소
        @SerializedName("ACMD_BULD_MNG_NO") val acmdBuldMngNo: String?,//수용건물관리번호
        @SerializedName("BDONG_CD") val bdongCd: String?,   //법정동코드
        @SerializedName("ARCD") val arcd: String?,  //지역코드
        @SerializedName("HDONG_CD") val hdongCd: String?, //행정동코드
//        @SerializedName("FCLTY_AR") val fcltyAr: Double?,  //시설면적
        @SerializedName("LA") val la: String?,  //위도
        @SerializedName("ACMDFCLTY_SN") val acmdfcltySn: Int?, //수용시설일련번호
        @SerializedName("EQK_ACMDFCLTY_ADRES") val eqkAcmdfcltyAdres: String?,  //지진옥외대피장소주소
        @SerializedName("VT_ACMD_PSBL_NMPR") val vtAcmdPsblNmpr: Int?   //이재민수용가능인원
    ) : Parcelable
}