package com.example.jikimi.data.model.dto

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "response")
data class NaturalDisasterResponse(
    @Element(name = "header") val header : Header,
    @Element(name = "body") val body : Body
)

@Xml(name = "header")
data class Header(
    @PropertyElement(name = "resultCode") val resultCode : String?,
    @PropertyElement(name = "resultMsg") val resultMsg : String?
)

@Xml(name = "body")
data class Body(
    @Element(name = "items") val items : Items,
    @PropertyElement(name = "numOfRows") val numOfRows : Int?,
    @PropertyElement(name = "pageNo") val pageNo : Int?,
    @PropertyElement(name = "totalCount") val totalCount : Int?, //총개수
)

@Xml(name = "items")
data class Items(
    @Element(name = "item") val item: List<Item>
)

@Xml(name = "item")
data class Item(
    @PropertyElement(name = "actRmks") val actRmks: String?,  //설명
    @PropertyElement(name = "contentsType") val contentsType: Int?,
    @PropertyElement(name = "mainOrd") val mainOrd: Int?,
    @PropertyElement(name = "safetyCate1") val safetyCate1: String?,
    @PropertyElement(name = "safetyCate2") val safetyCate2: String?,
    @PropertyElement(name = "safetyCate3") val safetyCate3: String?,
    @PropertyElement(name = "safetyCateNm1") val safetyCateNm1: String?,
    @PropertyElement(name = "safetyCateNm2") val safetyCateNm2: String?,
    @PropertyElement(name = "safetyCateNm3") val safetyCateNm3: String?,
    @PropertyElement(name = "subOrd") val subOrd: Int?,     // 리스트넘버
    @PropertyElement(name = "contentsUrl") val contentsUrl: String?   // URL
)
