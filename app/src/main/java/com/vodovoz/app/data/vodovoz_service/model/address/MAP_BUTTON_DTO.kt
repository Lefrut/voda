package com.vodovoz.app.data.vodovoz_service.model.address


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class MAP_BUTTON_DTO(
    @Json(name = "TEXT")
    val TEXT: String?,
    @Json(name = "KARTINKA")
    val KARTINKA: String?,
    @Json(name = "COLOR")
    val COLOR: String?,
    @Json(name = "BACKGROUND")
    val BACKGROUND: String?,
    @Json(name = "DATA")
    val DATA: MapPopupWindowDTO?
)