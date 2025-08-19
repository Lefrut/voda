package com.vodovoz.app.data.vodovoz_service.model.cart


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class ITOG_ITEM_DTO(
    @Json(name = "NAME")
    val name: String?,
    @Json(name = "VALUE")
    val value: String?,
    @Json(name = "VIDNO")
    val formattedValue: String?,
    @Json(name = "COLOR")
    val color: String?,
)