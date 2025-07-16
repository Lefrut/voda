package com.vodovoz.app.data.vodovoz_service.model.profile


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class BonusesPopupWindowDTO(
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "COUPON")
    val COUPON: String?,
    @Json(name = "OPISANIE")
    val OPISANIE: String?,
    @Json(name = "SGORANIE")
    val SGORANIE: BONUSES_SGORANIE_DTO?,
    @Json(name = "KNOPKA")
    val KNOPKA: KNOPKA_BONUSES_DTO?
)