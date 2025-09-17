package com.m.vodovoz.data.vodovoz_service.model.order


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class KLIENT_DTO(
    @Json(name = "TOCHKA")
    val TOCHKA: TOCHKA_DTO?
)