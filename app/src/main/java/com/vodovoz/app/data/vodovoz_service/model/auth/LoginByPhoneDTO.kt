package com.vodovoz.app.data.vodovoz_service.model.auth


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class LoginByPhoneDTO(
    @Json(name = "DATA")
    val DATA: LoginByPhoneDataDTO?,
    @Json(name = "TOKEN")
    val TOKEN: String?
)