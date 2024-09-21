package com.example.jikimi.data.model.dto

import com.google.gson.annotations.SerializedName

data class EarthquakeOutdoorsShelterResponse(
    @SerializedName("EarthquakeOutdoorsShelter2") val earthquakeOutdoorsShelter2: List<EarthquakeOutdoorsShelter2>
) {
    data class EarthquakeOutdoorsShelter2(
        @SerializedName("head") val head: List<Head>,
        @SerializedName("row") val row: List<Row>
    ) {
        data class Head(
            @SerializedName("totalCount") val totalCount: String?,
            @SerializedName("numOfRows") val numOfRows: String?,
            @SerializedName("pageNo") val pageNo: String?,
            @SerializedName("type") val type: String?,
            @SerializedName("RESULT") val result: RESULT?,
        ) {
            data class RESULT(
                @SerializedName("resultCode") val resultCode: String?,
                @SerializedName("resultMsg") val resultMsg: String?
            )
        }

        data class Row(
            @SerializedName("acmdfclty_se_nm") val acmdfcltySeNm: String?,
            @SerializedName("acmdfclty_sn") val acmdfcltySn: String?,
            @SerializedName("arcd") val arcd: String?,
            @SerializedName("bdong_cd") val bdongCd: String?,
            @SerializedName("ctprvn_nm") val ctprvnNm: String?,
            @SerializedName("dtl_adres") val dtlAdres: String?,
            @SerializedName("fclty_ar") val fcltyAr: String?,
            @SerializedName("hdong_cd") val hdongCd: String?,
            @SerializedName("mngps_nm") val mngpsNm: String?,
            @SerializedName("mngps_telno") val mngpsTelno: String?,
            @SerializedName("rdnmadr_cd") val rdnmadrCd: String?,
            @SerializedName("rn_adres") val rnAdres: String?,
            @SerializedName("sgg_nm") val sggNm: String?,
            @SerializedName("vt_acmd_psbl_nmpr") val vtAcmdPsblNmpr: String?,
            @SerializedName("vt_acmdfclty_nm") val vtAcmdfcltyNm: String?,
            @SerializedName("xcord") val xcord: String,
            @SerializedName("ycord") val ycord: String
        )
    }
}