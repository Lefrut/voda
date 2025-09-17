package com.m.vodovoz.data.vodovoz_service.model.auth


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class LoginByPhoneDataDTO(
    @Json(name = "user_id")
    val userId: Long?,
    @Json(name = "auth_status")
    val authStatus: Boolean?
)