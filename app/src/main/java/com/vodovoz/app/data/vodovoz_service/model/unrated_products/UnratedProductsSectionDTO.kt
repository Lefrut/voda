package com.vodovoz.app.data.vodovoz_service.model.unrated_products

import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class UnratedProductsSectionDTO(
    @Json(name = "TITLERAZDEL")
    val TITLERAZDEL: String?,

    @Json(name = "TITLETOVAR")
    val TITLETOVAR: String?,

    @Json(name = "LISTRAZDEL")
    val LISTRAZDEL: List<UnratedProductDTO>?,

    @Json(name = "VSEGOTOVAR")
    val VSEGOTOVAR: String?,

    @Json(name = "KNOPKA")
    val KNOPKA: UnratedProductsButton?,
)

@Keep
data class UnratedProductsButton(
    @Json(name = "NAME")
    val NAME: String,
    @Json(name = "ID")
    val ID: String?,
)

