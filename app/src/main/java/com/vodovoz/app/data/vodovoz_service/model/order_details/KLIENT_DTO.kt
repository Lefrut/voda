package com.vodovoz.app.data.vodovoz_service.model.order_details


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class KLIENT_DTO(
    @Json(name = "TOCHKA")
    val TOCHKA: TOCHKA_DTO?
)