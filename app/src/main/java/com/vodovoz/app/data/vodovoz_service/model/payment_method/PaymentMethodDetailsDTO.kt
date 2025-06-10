package com.vodovoz.app.data.vodovoz_service.model.payment_method


import com.squareup.moshi.Json
import androidx.annotation.Keep
import com.vodovoz.app.data.vodovoz_service.model.auth.KNOPKA_AUTH_DTO

@Keep
data class PaymentMethodDetailsDTO(
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "DANNYE")
    val DANNYE: List<PaymentMethodSectionDTO>?,
    @Json(name = "KNOPKA")
    val KNOPKA: KNOPKA_AUTH_DTO?
)