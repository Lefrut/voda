package com.vodovoz.app.data.vodovoz_service.model


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class CATEGORY_WITH_PRODUCTS_DTO(
    @Json(name = "data")
    val data: List<TOVAR_DATA_DTO>?,
    @Json(name = "ID")
    val ID: Long?,
    @Json(name = "NAME")
    val NAME: String?
)