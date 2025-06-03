package com.vodovoz.app.data.vodovoz_service.model.user_data


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class SPISOK(
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "ID")
    val ID: String?
)