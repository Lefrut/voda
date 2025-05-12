package com.vodovoz.app.data.vodovoz_service.model.auth


import com.squareup.moshi.Json
import androidx.annotation.Keep
import java.time.LocalDateTime

@Keep
data class RequestCodeDTO(
    @Json(name = "data")
    val data: LocalDateTime?,
    @Json(name = "data_now")
    val dataNow: LocalDateTime?,
    @Json(name = "time")
    val time: String?,
    @Json(name = "status_code")
    val statusCode: String?
)