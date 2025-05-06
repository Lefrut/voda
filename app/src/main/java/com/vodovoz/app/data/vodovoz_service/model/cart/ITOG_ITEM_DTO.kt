package com.vodovoz.app.data.vodovoz_service.model.cart


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class ITOG_ITEM_DTO(
    @Json(name = "NAME")
    val name: String?,
    @Json(name = "VALUE")
    val value: String?,
    @Json(name = "COLOR")
    val color: String?,
)