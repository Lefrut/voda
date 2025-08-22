package com.vodovoz.app.data.vodovoz_service.model.order

import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class ORDER_POLYSHATEL_ITEM_DTO(
    @Json(name = "KARTINKA")
    val KARTINKA: String?,
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "OPISANIE")
    val OPISANIE: String?,
    @Json(name = "ID")
    val ID: String?,
    @Json(name = "DEFAULTVALUE")
    val DEFAULTVALUE: String?,
)