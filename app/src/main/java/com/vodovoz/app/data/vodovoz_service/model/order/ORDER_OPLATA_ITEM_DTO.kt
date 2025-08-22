package com.vodovoz.app.data.vodovoz_service.model.order


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class ORDER_OPLATA_ITEM_DTO(
    @Json(name = "KARTINKA")
    val KARTINKA: String?,
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "OPISANIE")
    val OPISANIE: String?,
    @Json(name = "ID")
    val ID: String?
)