package com.vodovoz.app.data.vodovoz_service.model.auth


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class LoginByPhoneDTO(
    @Json(name = "DATA")
    val DATA: LoginByPhoneDataDTO?,
    @Json(name = "TOKEN")
    val TOKEN: String?
)