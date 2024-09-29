package com.example.jikimi.data.model.dto


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class EarthquakeIndoorsShelterResponse(
    @SerializedName("EarthquakeIndoors") val earthquakeIndoors: List<EarthquakeIndoor>
) {
    data class EarthquakeIndoor(
        @SerializedName("head") val head: List<Head>,
        @SerializedName("row") val row: List<Row>
    ) {
        data class Head(
            @SerializedName("totalCount") val totalCount: String?,
            @SerializedName("numOfRows") val numOfRows: String?,
            @SerializedName("pageNo") val pageNo: String?,
            @SerializedName("type") val type: String?,
            @SerializedName("RESULT") val result: RESULT?
        ) {
            data class RESULT(
                @SerializedName("resultCode") val resultCode: String?,
                @SerializedName("resultMsg") val resultMsg: String?
            )
        }

        @Parcelize
        data class Row(
            @SerializedName("arcd") val arcd: String?,
            @SerializedName("acmdfclty_sn") val acmdfcltySn: String?,
            @SerializedName("ctprvn_nm") val ctprvnNm: String?,
            @SerializedName("sgg_nm") val sggNm: String?,
            @SerializedName("vt_acmdfclty_nm") val vtAcmdfcltyNm: String?,
            @SerializedName("rdnmadr_cd") val rdnmadrCd: String?,
            @SerializedName("bdong_cd") val bdongCd: String?,
            @SerializedName("hdong_cd") val hdongCd: String?,
            @SerializedName("dtl_adres") val dtlAdres: String?,
            @SerializedName("fclty_ar") val fcltyAr: String?,
            @SerializedName("xcord") val xcord: String,
            @SerializedName("ycord") val ycord: String,
            @SerializedName("mngps_nm") val mngpsNm: String?,
            @SerializedName("mngps_telno") val mngpsTelno: String?,
            @SerializedName("acmdfclty_dtl_cn") val acmdfcltyDtlCn: String?,
            @SerializedName("rn_adres") val rnAdres: String?,
            @SerializedName("mngdpt_nm") val mngdptNm: String?,
            @SerializedName("vt_acmd_psbl_nmpr") val vtAcmdPsblNmpr: String?
        ) : Parcelable
    }
}