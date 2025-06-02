package com.vodovoz.app.data.vodovoz_service.model.order_history


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class ORDERS_HISTORY_KNOPKA_DTO(
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "IMAGE")
    val IMAGE: String?,
    @Json(name = "COLOR_BACKGROUND")
    val COLOR_BACKGROUND: String?,
    @Json(name = "COLOR_TEXT")
    val COLOR_TEXT: String?,
    @Json(name = "URL")
    val URL: String?,
    @Json(name = "BRAYZER")
    val BRAYZER: String?,
    @Json(name = "ID")
    val ID: String?
)