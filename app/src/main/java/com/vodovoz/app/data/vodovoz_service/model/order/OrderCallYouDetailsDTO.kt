package com.vodovoz.app.data.vodovoz_service.model.order


import com.squareup.moshi.Json
import androidx.annotation.Keep
import com.vodovoz.app.data.vodovoz_service.model.KNOPKA_ORDER_DTO

@Keep
data class OrderCallYouDetailsDTO(
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "DANNYE")
    val DANNYE: List<CallYouItemDTO>?,
    @Json(name = "KNOPKA")
    val KNOPKA: KNOPKA_ORDER_DTO?
)