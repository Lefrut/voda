package com.m.vodovoz.data.vodovoz_service.model.order


import com.squareup.moshi.Json
import androidx.annotation.Keep
import com.m.vodovoz.data.vodovoz_service.model.CHECKBOX_DTO
import com.m.vodovoz.data.vodovoz_service.model.KNOPKA_ORDER_DTO
import com.m.vodovoz.data.vodovoz_service.model.user_data.POLE_DTO

@Keep
data class RecipientDetailsDTO(
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "POLYA")
    val POLYA: List<POLE_DTO>?,
    @Json(name = "CHECKBOX")
    val CHECKBOX: List<CHECKBOX_DTO>?,
    @Json(name = "KNOPKA")
    val KNOPKA: KNOPKA_ORDER_DTO?
)