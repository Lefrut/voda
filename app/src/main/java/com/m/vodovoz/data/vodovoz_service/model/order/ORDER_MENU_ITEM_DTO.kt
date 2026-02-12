package com.m.vodovoz.data.vodovoz_service.model.order

import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class ORDER_MENU_ITEM_DTO(
    @Json(name = "KARTINKA")
    val KARTINKA: String?,
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "OPISANIE")
    val OPISANIE: String?,
    @Json(name = "ID")
    val ID: String?,
    @Json(name = "TYPE")
    val TYPE: String?,
    @Json(name = "DEFAULTVALUE")
    val DEFAULTVALUE: String?,
    @Json(name = "DEFAULT")
    val DEFAULT: Boolean?,
    @Json(name = "DOPOKNO")
    val DOPOKNO: ORDERING_COMMENT_OKNO?,
)