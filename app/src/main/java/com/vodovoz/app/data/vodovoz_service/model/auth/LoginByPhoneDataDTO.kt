package com.vodovoz.app.data.vodovoz_service.model.auth


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class LoginByPhoneDataDTO(
    @Json(name = "user_id")
    val userId: Long?,
    @Json(name = "auth_status")
    val authStatus: Boolean?
)