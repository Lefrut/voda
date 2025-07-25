package com.vodovoz.app.data.vodovoz_service.model.user_data


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class UserDataDTO(
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "FOTO")
    val FOTO: FOTO_DTO?,
    @Json(name = "POLYA")
    val POLYA: List<POLE_DTO>?,
    @Json(name = "TEXT")
    val TEXT: USER_DATA_TEXT_DTO?
)