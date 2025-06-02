package com.vodovoz.app.data.vodovoz_service.model


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class KNOPKA_DTO(
    @Json(name = "ID")
    val ID: String?,
    @Json(name = "NAME")
    val NAME: String?
)

@Keep
data class KNOPKA_INT_DTO(
    @Json(name = "ID")
    val ID: Int?,
    @Json(name = "NAME")
    val NAME: String?
)