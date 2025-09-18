package com.m.vodovoz.data.vodovoz_service.model


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class SuperTopAndBottomSectionsDTO(
    @Json(name = "RAZDEL_NIZ")
    val RAZDEL_NIZ: SUPER_TOP_RAZDEL?,
    @Json(name = "RAZDEL_VERH")
    val RAZDEL_VERH: SUPER_TOP_RAZDEL?
)