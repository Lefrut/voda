package com.m.vodovoz.data.vodovoz_service.model


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class TARA_DTO(
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "ID")
    val ID: Long?,
    @Json(name = "PROPERTY_CML2_ARTICLE_VALUE")
    val PROPERTY_CML2_ARTICLE_VALUE: String?,
    @Json(name = "OPISANIE")
    val OPISANIE: String?
)