package com.vodovoz.app.data.vodovoz_service.model


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class CATEGORY_NODE_DTO(
    @Json(name = "ID")
    val ID: Long?,
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "DEPTH_LEVEL")
    val DEPTH_LEVEL: Int?,
    @Json(name = "PODRAZDEL")
    val PODRAZDEL: List<CATEGORY_NODE_DTO>?,
    @Json(name = "SUBSECTIONS")
    val SUBSECTIONS: Int?
)