package com.m.vodovoz.data.vodovoz_service.model.product_details


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class HARAKTERISTIK_BIND_DTO(
    @Json(name = "CODE")
    val CODE: String?,
    @Json(name = "HINT")
    val HINT: String?,
    @Json(name = "ID")
    val ID: Int?,
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "VALUE")
    val VALUE: String?
)