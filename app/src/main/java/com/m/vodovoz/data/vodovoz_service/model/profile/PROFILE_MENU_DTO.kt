package com.m.vodovoz.data.vodovoz_service.model.profile


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class PROFILE_MENU_DTO(
    @Json(name = "MINI")
    val MINI: List<PROFILE_MINI_MENU_DTO>?,
    @Json(name = "NORMAL")
    val NORMAL: List<PROFILE_NORMAL_MENU_DTO>?
)