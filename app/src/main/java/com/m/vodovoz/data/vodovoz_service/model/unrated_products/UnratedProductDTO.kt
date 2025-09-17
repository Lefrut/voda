package com.m.vodovoz.data.vodovoz_service.model.unrated_products

import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json

@Keep
data class UnratedProductDTO(
    @Json(name = "NAME")
    val NAME: String?,

    @Json(name = "ID")
    val ID: Long?,

    @Json(name = "DETAIL_PICTURE")
    val DETAIL_PICTURE: String?
)