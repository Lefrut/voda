package com.vodovoz.app.data.vodovoz_service.model


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class SuperTopAndBottomSectionsDTO(
    @Json(name = "RAZDEL_NIZ")
    val RAZDEL_NIZ: CATEGORY_RAZDEL?,
    @Json(name = "RAZDEL_VERH")
    val RAZDEL_VERH: CATEGORY_RAZDEL?
)