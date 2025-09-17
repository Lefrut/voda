package com.m.vodovoz.data.vodovoz_service.model.order


import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.m.vodovoz.data.vodovoz_service.model.COLORFUL_KNOPKA_DTO
import com.m.vodovoz.data.vodovoz_service.model.cart.ITOG_ITEM_DTO

@Keep
data class OrderDetailsDTO(
    @Json(name = "TITLE")
    val TITLE: ORDER_DETAILS_TITLE_DTO?,
    @Json(name = "BLOCK")
    val BLOCK: ORDER_DETAILS_BLOCK_DTO?,
    @Json(name = "TOVARY")
    val TOVARY: ORDER_DETAILS_TOVARY_DTO?,
    @Json(name = "ITOG")
    val ITOG: List<ITOG_ITEM_DTO>?,
    @Json(name = "KNOPKI_NIZ")
    val KNOPKI_NIZ: List<COLORFUL_KNOPKA_DTO>?
)