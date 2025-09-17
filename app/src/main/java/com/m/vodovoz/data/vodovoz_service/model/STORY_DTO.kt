package com.m.vodovoz.data.vodovoz_service.model


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class STORY_DTO(
    @Json(name = "ID")
    val ID: Long?,
    @Json(name = "RAZDEL")
    val RAZDEL: STORY_IMAGE_DTO?,
    @Json(name = "VNYTRENNOST")
    val VNYTRENNOST: List<VNYTRENNOST_DTO?>?,
    @Json(name = "VNYTRENNOSCOUNT")
    val VNYTRENNOSTCOUNT: Int?
)