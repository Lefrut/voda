package com.m.vodovoz.data.vodovoz_service.model.profile


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class PROFILE_MENO_OKNO_ID_DTO(
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "OPISANIE")
    val OPISANIE: String?,
    @Json(name = "MENU")
    val MENU: List<CHAT_MENU_DTO?>?
)