package com.vodovoz.app.data.vodovoz_service.model.order_details


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class VODITEL_DTO(
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "TOCHKA")
    val TOCHKA: TOCHKA_DTO?,
    @Json(name = "DANNYE")
    val DANNYE: List<IMAGE_AND_TEXT_DTO>?,
    @Json(name = "KNOPKI")
    val KNOPKI: List<WHERE_ORDER_BUTTON_DTO>?
)