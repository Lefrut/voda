package com.m.vodovoz.data.vodovoz_service.model.certificate


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class SERTIFICAT_DTO(
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "OBAZATELEN")
    val OBAZATELEN: String?,
    @Json(name = "CODE")
    val CODE: String?,
    @Json(name = "VID")
    val VID: List<SERTIFICAT_VID_DTO>?,
    @Json(name = "TEXT")
    val TEXT: String?
)