package com.vodovoz.app.data.vodovoz_service.model.certificate


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class SERTIFICAT_VID_DTO(
    @Json(name = "ID")
    val ID: Int?,
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "PICTURE")
    val PICTURE: String?
)