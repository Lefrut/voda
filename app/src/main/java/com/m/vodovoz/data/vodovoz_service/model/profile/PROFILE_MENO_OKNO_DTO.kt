package com.m.vodovoz.data.vodovoz_service.model.profile


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class PROFILE_MENO_OKNO_DTO(
    @Json(name = "ID")
    val ID: PROFILE_MENO_OKNO_ID_DTO?
)