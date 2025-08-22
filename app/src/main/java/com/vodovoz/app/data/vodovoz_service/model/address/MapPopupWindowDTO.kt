package com.vodovoz.app.data.vodovoz_service.model.address


import com.squareup.moshi.Json
import androidx.annotation.Keep
import com.vodovoz.app.data.vodovoz_service.model.order.IMAGE_AND_TEXT_DTO

@Keep
data class MapPopupWindowDTO(
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "OPISANIE")
    val OPISANIE: String?,
    @Json(name = "TOVAR")
    val TOVAR: List<IMAGE_AND_TEXT_DTO>?
)