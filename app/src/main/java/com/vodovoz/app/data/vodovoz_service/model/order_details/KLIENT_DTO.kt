package com.vodovoz.app.data.vodovoz_service.model.order_details


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class KLIENT_DTO(
    @Json(name = "TOCHKA")
    val TOCHKA: TOCHKA_DTO?
)