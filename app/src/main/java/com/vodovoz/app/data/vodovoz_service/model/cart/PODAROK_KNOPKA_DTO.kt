package com.vodovoz.app.data.vodovoz_service.model.cart


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class PODAROK_KNOPKA_DTO(
    @Json(name = "TEXT")
    val TEXT: String?,
    @Json(name = "COLOR")
    val COLOR: String?,
    @Json(name = "BACKGROUND")
    val BACKGROUND: String?,
    @Json(name = "ID")
    val ID: String?
)