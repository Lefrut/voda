package com.vodovoz.app.data.vodovoz_service.model.ordering


import com.squareup.moshi.Json
import androidx.annotation.Keep
import com.vodovoz.app.data.vodovoz_service.model.cart.ITOG_ITEM_DTO
import com.vodovoz.app.data.vodovoz_service.model.cart.PODAROK_KNOPKA_DTO
import com.vodovoz.app.design_system.model.ColorfulButtonUi

@Keep
data class OrderingDetailsDTO(
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "POLYSHATEL")
    val POLYSHATEL: ORDER_POLYSHATEL_DTO?,
    @Json(name = "KOMMENT")
    val KOMMENT: ORDER_KOMMENT_DTO?,
    @Json(name = "OPLATA")
    val OPLATA: ORDER_OPLATA_DTO?,
    @Json(name = "ITOG")
    val ITOG: List<ITOG_ITEM_DTO>?,
    @Json(name = "KNOPKA")
    val KNOPKA: PODAROK_KNOPKA_DTO?
)