package com.vodovoz.app.data.vodovoz_service.model.certificate


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class FAQ_ITEM_DTO(
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "OPISANIE")
    val OPISANIE: String?
)