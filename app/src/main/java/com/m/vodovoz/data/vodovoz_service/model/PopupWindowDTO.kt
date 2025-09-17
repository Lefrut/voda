package com.m.vodovoz.data.vodovoz_service.model


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class PopupWindowDTO(
    @Json(name = "BANNER")
    val BANNER: List<SPECTIAL_PROMOTION_DTO>?,
    @Json(name = "UPDATE")
    val UPDATE: APP_UPDATE_INFO_DTO?
)