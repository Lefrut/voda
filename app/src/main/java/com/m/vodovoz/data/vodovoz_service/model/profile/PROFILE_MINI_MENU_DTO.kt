package com.m.vodovoz.data.vodovoz_service.model.profile


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class PROFILE_MINI_MENU_DTO(
    @Json(name = "TEXT")
    val TEXT: String?,
    @Json(name = "IMAGE")
    val IMAGE: String?,
    @Json(name = "ID")
    val ID: String?
)