package com.m.vodovoz.data.vodovoz_service.model.auth


import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.m.vodovoz.data.vodovoz_service.model.CHECKBOX_DTO
import com.m.vodovoz.data.vodovoz_service.model.user_data.POLE_DTO

@Keep
data class AuthDetailsDTO(
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "OPISANIE")
    val OPISANIE: String?,
    @Json(name = "BIZNES")
    val BIZNES: AccountTypeSectionDTO?,
    @Json(name = "DATA")
    val DATA: List<POLE_DTO>?,
    @Json(name = "SOGLASHENIE")
    val SOGLASHENIE: String?,
    @Json(name = "KNOPKA")
    val KNOPKA: List<KNOPKA_AUTH_DTO>?,
    @Json(name = "PODOFERTA")
    val PODOFERTA: CHECKBOX_DTO?,
    @Json(name = "PODPISKA")
    val PODPISKA: List<CHECKBOX_DTO>?
)
