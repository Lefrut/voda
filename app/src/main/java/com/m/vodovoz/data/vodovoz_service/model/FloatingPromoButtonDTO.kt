package com.m.vodovoz.data.vodovoz_service.model

import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class FloatingPromoButtonDTO(
    @Json(name = "DETAIL_PICTURE")
    val DETAIL_PICTURE: String?,
    @Json(name = "ID")
    val ID: Int?,
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "IBLOCK_ID")
    val IBLOCK_ID: Int?,
    @Json(name = "HARAKTERISTIK")
    val HARAKTERISTIK: ACTION_DTO?,
    @Json(name = "POKAZ")
    val POKAZ: FloatingButtonVisibilityDTO?,
    @Json(name = "OREKLAME")
    val OREKLAME: Any?,
)

@Keep
data class FloatingButtonVisibilityDTO(
    @Json(name = "RIGHT")
    val RIGHT: List<String>?,
    @Json(name = "LEFT")
    val LEFT: List<String>?,
)