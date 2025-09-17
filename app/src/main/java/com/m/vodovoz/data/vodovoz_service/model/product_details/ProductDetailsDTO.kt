package com.m.vodovoz.data.vodovoz_service.model.product_details


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class ProductDetailsDTO(
    @Json(name = "BLOCTOVAR")
    val BLOCTOVAR: List<TOVAR_SECTION_DTO>?,
    @Json(name = "COMMENTS")
    val COMMENTS: COMMENTS_DTO?,
    @Json(name = "DETAILTEXT")
    val DETAILTEXT: List<DETAILTEXT_DTO>?,
    @Json(name = "KNOPKI")
    val KNOPKI: PRODUCT_DETAILS_KNOPKI_DTO?,
    @Json(name = "SHARE")
    val SHARE: PODILITSYA_DTO?,
    @Json(name = "TOVAR")
    val TOVAR: TOVAR_DETAIL_DTO?
)