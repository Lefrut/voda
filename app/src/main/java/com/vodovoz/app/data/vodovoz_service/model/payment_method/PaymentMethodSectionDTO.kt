package com.vodovoz.app.data.vodovoz_service.model.payment_method


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class PaymentMethodSectionDTO(
    @Json(name = "ZAGALOVOK")
    val ZAGALOVOK: String?,
    @Json(name = "OPLATA")
    val OPLATA: List<PaymentMethodItemDTO>?
)