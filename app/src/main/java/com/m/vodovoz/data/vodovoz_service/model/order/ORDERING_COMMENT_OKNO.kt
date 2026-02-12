package com.m.vodovoz.data.vodovoz_service.model.order

import androidx.annotation.Keep
import com.m.vodovoz.data.vodovoz_service.model.KNOPKA_ORDER_DTO
import com.squareup.moshi.Json

@Keep
data class ORDERING_COMMENT_OKNO(
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "OPISANIE")
    val OPISANIE: String?,
    @Json(name = "HINT")
    val HINT: String?,
    @Json(name = "VALUE")
    val VALUE: String?,
    @Json(name = "KNOPKA")
    val KNOPKA: KNOPKA_ORDER_DTO?
)