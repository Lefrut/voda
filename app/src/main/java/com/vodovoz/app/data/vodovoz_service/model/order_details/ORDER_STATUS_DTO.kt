package com.vodovoz.app.data.vodovoz_service.model.order_details


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class ORDER_STATUS_DTO(
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "ID")
    val ID: String?,
    @Json(name = "BACKGROUND")
    val BACKGROUND: String?,
    @Json(name = "COLOR")
    val COLOR: String?,
    @Json(name = "IMAGE")
    val IMAGE: String?,
    @Json(name = "BACKGROUNDOPACITY")
    val BACKGROUNDOPACITY: Float?
)