package com.example.jikimi.data.model.dto

import android.os.Parcelable
import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Xml
import com.tickaroo.tikxml.annotation.PropertyElement
import kotlinx.parcelize.Parcelize


@Xml(name = "response")
data class NaturalDisasterResponse(
    @Element(name = "header") val header : Header?,
    @Element(name = "body") val body : Body?,
)


@Xml(name = "header")
data class Header(
    @PropertyElement(name = "resultCode") val resultCode : String?,
    @PropertyElement(name = "resultMsg") val resultMsg : String?
)

@Xml(name = "body")
data class Body(
    @Element(name = "items") val items : Items?,
    @PropertyElement(name = "numOfRows") val numOfRows : Int ,
    @PropertyElement(name = "pageNo") val pageNo : Int ,
    @PropertyElement(name = "totalCount") val totalCount : Int , //총개수
)

@Xml(name = "items")
data class Items(
    @Element(name = "item") val item: List<Item>?
)

@Parcelize
@Xml(name = "item")
data class Item(
    @PropertyElement(name = "actRmks") var actRmks: String?,  //설명
    @PropertyElement(name = "contentsType") var contentsType: Int?,
    @PropertyElement(name = "mainOrd") var mainOrd: Int?,
    @PropertyElement(name = "safetyCate1") var safetyCate1: String?,
    @PropertyElement(name = "safetyCate2") var safetyCate2: String?,
    @PropertyElement(name = "safetyCate3") var safetyCate3: String?,
    @PropertyElement(name = "safetyCateNm1") var safetyCateNm1: String?,
    @PropertyElement(name = "safetyCateNm2") var safetyCateNm2: String?,
    @PropertyElement(name = "safetyCateNm3") var safetyCateNm3: String?,
    @PropertyElement(name = "subOrd") var subOrd: Int?,     // 리스트넘버
    @PropertyElement(name = "contentsUrl") var contentsUrl: String?   // URL
) : Parcelable
