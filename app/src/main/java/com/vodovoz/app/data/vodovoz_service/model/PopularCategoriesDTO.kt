package com.vodovoz.app.data.vodovoz_service.model


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class PopularCategoriesDTO(
    @Json(name = "LISTRAZDEL")
    val LISTRAZDEL: List<POPULAR_CATEGORY_DTO?>?,
    @Json(name = "TITLERAZDEL")
    val TITLERAZDEL: String?
)