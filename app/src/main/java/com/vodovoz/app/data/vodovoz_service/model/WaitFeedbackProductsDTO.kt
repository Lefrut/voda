package com.vodovoz.app.data.vodovoz_service.model

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Keep
data class WaitFeedbackProductsDTO(
    @Json(name = "TITLE")
    val title: String?,
    @Json(name = "LISTRAZDEL")
    val products: List<WaitFeedbackProductDTO>?
)
