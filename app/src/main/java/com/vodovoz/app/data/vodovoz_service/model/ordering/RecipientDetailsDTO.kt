package com.vodovoz.app.data.vodovoz_service.model.ordering


import com.squareup.moshi.Json
import androidx.annotation.Keep
import com.vodovoz.app.data.vodovoz_service.model.KNOPKA_ORDER_DTO
import com.vodovoz.app.data.vodovoz_service.model.user_data.POLE_DTO

@Keep
data class RecipientDetailsDTO(
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "POLYA")
    val POLYA: List<POLE_DTO>?,
    @Json(name = "KNOPKA")
    val KNOPKA: KNOPKA_ORDER_DTO?
)