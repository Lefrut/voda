package com.vodovoz.app.data.vodovoz_service.model.order


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class ORDER_DETAILS_BLOCK_DTO(
    @Json(name = "GLAV")
    val GLAV: String?,
    @Json(name = "STATUS")
    val STATUS: List<ORDER_STATUS_DTO>?,
    @Json(name = "STATUSY")
    val STATUSY: List<ORDER_STATUS_DTO>?,
    @Json(name = "KNOPKI")
    val KNOPKI: List<ORDER_DETAILS_KNOPKA_DTO>?,
    @Json(name = "KNOPKIOPIS")
    val KNOPKIOPIS: List<ORDER_DETAILS_KNOPKA_DTO>?,
)