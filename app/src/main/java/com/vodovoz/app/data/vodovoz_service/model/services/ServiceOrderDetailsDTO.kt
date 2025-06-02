package com.vodovoz.app.data.vodovoz_service.model.services


import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.vodovoz.app.data.vodovoz_service.model.KNOPKA_ORDER_DTO
import com.vodovoz.app.data.vodovoz_service.model.user_data.POLE_DTO

@Keep
data class ServiceOrderDetailsDTO(
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "INFORMIROVANIE")
    val INFORMIROVANIE: String?,
    @Json(name = "LISTADATA")
    val LISTADATA: List<POLE_DTO>?,
    @Json(name = "KNOPKA")
    val KNOPKA: KNOPKA_ORDER_DTO?
)