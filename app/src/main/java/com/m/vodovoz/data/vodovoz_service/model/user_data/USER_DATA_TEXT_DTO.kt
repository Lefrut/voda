package com.m.vodovoz.data.vodovoz_service.model.user_data

import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class USER_DATA_TEXT_DTO(
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "TEXTID")
    val TEXTID: String?
)
