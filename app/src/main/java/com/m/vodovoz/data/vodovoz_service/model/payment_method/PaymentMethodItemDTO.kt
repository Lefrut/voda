package com.m.vodovoz.data.vodovoz_service.model.payment_method


import com.squareup.moshi.Json
import androidx.annotation.Keep
import com.m.vodovoz.data.vodovoz_service.model.user_data.POLE_DTO

@Keep
data class PaymentMethodItemDTO(
    @Json(name = "KARTINKA")
    val KARTINKA: String?,
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "ID")
    val ID: String?,
    @Json(name = "CODE")
    val CODE: String?,
    @Json(name = "POLE")
    val POLE: POLE_DTO?
)