package com.myapp.jikimi.data.model.dto

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
        @SerializedName("DTL_ADRES") val outoorDetailAddress: String?, //상세주소
        @SerializedName("LO") val outdoorLatitude: String?,              //경도
        @SerializedName("VT_ACMDFCLTY_NM") val outdoorShelterName: String?,  //야외시설명
        @SerializedName("RN_DTL_ADRES") val outdoorRoadAddress: String?,    //도로명상세주소
        @SerializedName("LA") val outdoorLongitude: String?,                //위도
        @SerializedName("EQK_ACMDFCLTY_ADRES") val outoorAddress: String?,  //지진옥외대피장소주소
        @SerializedName("VT_ACMD_PSBL_NMPR") val outdoorcapacityNumber: String?   //이재민수용가능인원
    ) : Parcelable
}