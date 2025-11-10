package com.m.vodovoz.data.vodovoz_service.model.cart

import com.m.vodovoz.data.vodovoz_service.model.COLORFUL_KNOPKA_DTO
import com.m.vodovoz.data.vodovoz_service.model.TOVAR_DATA_DTO
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecommendationsDTO(
    @Json(name = "TITLE")
    val title: String?,
    @Json(name = "OPISANIE")
    val description: String?,
    @Json(name = "TOVARY")
    val products: List<TOVAR_DATA_DTO>?,
    @Json(name = "COUNT")
    val countProducts: Int?,
    @Json(name = "STRANIC")
    val countPages: Int?,
    @Json(name = "TOVARVSEGO")
    val totalProducts: Int?,
    @Json(name = "KNOPKA")
    val button: COLORFUL_KNOPKA_DTO?
)