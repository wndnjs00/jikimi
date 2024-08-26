package com.example.jikimi.data.model.dto

import com.google.gson.annotations.SerializedName

// API로 받아온 데이터클래스 가져옴

data class EarthquakeOutdoorsShelterResponse(
    @SerializedName("EarthquakeOutdoorsShelter2") val earthquakeOutdoorsShelter2: List<EarthquakeOutdoorsShelter>
)

data class EarthquakeOutdoorsShelter(
    @SerializedName("head") val head: List<Head>,
    @SerializedName("row") val row: List<Row>
)

data class Head(
    @SerializedName("totalCount") val totalCount: String?,
    @SerializedName("numOfRows") val numOfRows: String?,
    @SerializedName("pageNo") val pageNo: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("RESULT") val result: Result?
)

data class Result(
    @SerializedName("resultCode") val resultCode: String?,
    @SerializedName("resultMsg") val resultMsg: String?
)

data class Row(
    @SerializedName("arcd") val arcd: String?,                  //지역코드
    @SerializedName("acmdfclty_sn") val acmdfcltySn: String?,      //시설일련번호
    @SerializedName("ctprvn_nm") val ctprvnNm: String?,         // 시,도 명
    @SerializedName("sgg_nm") val sggNm: String?,               // 시,군,구 명
    @SerializedName("vt_acmdfclty_nm") val vtAcmdfcltyNm: String?,      // 대피시설명
    @SerializedName("rdnmadr_cd") val rdnmadrCd: String?,        // 도로명주소 코드
    @SerializedName("bdong_cd") val bdongCd: String?,            // 법정동 코드
    @SerializedName("hdong_cd") val hdongCd: String?,           // 행정동 코드
    @SerializedName("dtl_adres") val dtlAdres: String?,         // 상세 주소
    @SerializedName("fclty_ar") val fcltyAr: String?,              // 시설 면적
    @SerializedName("xcord") val xcord: String?,                //경도    // 128.36909675060997
    @SerializedName("ycord") val ycord: String?,                //위도    // 36.986562674118154
    @SerializedName("mngps_nm") val mngpsNm: String?,           // 기관명
    @SerializedName("mngps_telno") val mngpsTelno: String?,     // 기관전화번호
    @SerializedName("vt_acmd_psbl_nmpr") val vtAcmdPsblNmpr: String?,   // 최대수용인원수
    @SerializedName("acmdfclty_se_nm") val acmdfcltySeNm: String?,   // 유형
    @SerializedName("rn_adres") val rnAdres: String?                 // 도로명 주소
)