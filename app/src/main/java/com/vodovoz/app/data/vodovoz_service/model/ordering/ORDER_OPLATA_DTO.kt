package com.vodovoz.app.data.vodovoz_service.model.ordering


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import androidx.annotation.Keep

@Keep
@JsonClass(generateAdapter = true)
data class ORDER_OPLATA_DTO(
    @Json(name = "ZAGOLOVOK")
    val ZAGOLOVOK: String?,
    @Json(name = "DANNYE")
    val DANNYE: List<ORDER_OPLATA_ITEM_DTO>?
)