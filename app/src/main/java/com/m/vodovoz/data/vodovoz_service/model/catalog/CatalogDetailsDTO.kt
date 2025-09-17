package com.m.vodovoz.data.vodovoz_service.model.catalog


import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.m.vodovoz.data.vodovoz_service.model.BannerDTO

@Keep
data class CatalogDetailsDTO(
    @Json(name = "BANNER")
    val BANNER: List<BannerDTO?>?,
    @Json(name = "RAZDEL")
    val RAZDEL: List<CATALOG_CATEGORY_DTO?>?
)