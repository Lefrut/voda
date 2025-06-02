package com.vodovoz.app.data.vodovoz_service.model


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class AllBottlesDetailsDTO(
    @Json(name = "OPISANIE")
    val OPISANIE: String?,
    @Json(name = "KPOPKAPLUS")
    val KPOPKAPLUS: String?,
    @Json(name = "TARA")
    val TARA: List<TARA_DTO>?
)