package com.vodovoz.app.data.vodovoz_service.model.product_details


import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.vodovoz.app.data.vodovoz_service.model.auth.KNOPKA_AUTH_DTO

@Keep
data class PRODUCT_DETAILS_KNOPKI_DTO(
    @Json(name = "DESHEVLE")
    val DESHEVLE: KNOPKA_DESHEVLE_DTO?,
    @Json(name = "ANALOG")
    val ANALOG: KNOPKA_AUTH_DTO?,
    @Json(name = "ZAKAZAT")
    val ZAKAZAT: KNOPKA_AUTH_DTO?
)