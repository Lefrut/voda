package com.vodovoz.app.data.vodovoz_service.model.order_details


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class WhereMyOrderDetailsDTO(
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "VODITEL")
    val VODITEL: VODITEL_DTO?,
    @Json(name = "KLIENT")
    val KLIENT: KLIENT_DTO?,
)