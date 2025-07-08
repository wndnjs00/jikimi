package com.myapp.jikimi.data.model.dto


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class EarthquakeIndoorsShelterResponse(
    @SerializedName("EarthquakeIndoors") val earthquakeIndoors: List<EarthquakeIndoor>
) {
    data class EarthquakeIndoor(
        @SerializedName("head") val head: List<Head> = emptyList(),
        @SerializedName("row") val row: List<Row> = emptyList()
    ) {
        data class Head(
            @SerializedName("totalCount") val totalCount: String?,
            @SerializedName("numOfRows") val numOfRows: String? = null,
            @SerializedName("pageNo") val pageNo: String? = null,
            @SerializedName("type") val type: String? = null,
            @SerializedName("RESULT") val result: RESULT? = null
        ) {
            data class RESULT(
                @SerializedName("resultCode") val resultCode: String?,
                @SerializedName("resultMsg") val resultMsg: String?
            )
        }

        @Parcelize
        data class Row(
            @SerializedName("vt_acmdfclty_nm") val indoorShelterName: String?,  // 임시주거시설명
            @SerializedName("dtl_adres") val indoorAddress: String?,  //지번주소
            @SerializedName("xcord") val indoorLatitude: String,     //경도
            @SerializedName("ycord") val indoorLongitude: String,     //위도
            @SerializedName("mngps_telno") val indoorPhoneNumber: String?,   //관리기관전화번호
            @SerializedName("acmdfclty_dtl_cn") val indoorDetailAddress: String?,   // 지진겸용 임시주거시실 상세시설명
            @SerializedName("rn_adres") val indoorRoadAddress: String?,             // 도로명주소
            @SerializedName("vt_acmd_psbl_nmpr") val indoorcapacityNumber: String?  //최대수용인원수
        ) : Parcelable
    }
}