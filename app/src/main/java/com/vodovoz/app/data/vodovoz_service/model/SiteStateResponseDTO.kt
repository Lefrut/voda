package com.vodovoz.app.data.vodovoz_service.model


import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Keep
data class SiteStateResponseDTO(
    @Json(name = "ACTIVE")
    val ACTIVE: String?,
    @Json(name = "CHATJIVO")
    val CHATJIVO: CHATJIVO_DTO?,
    @Json(name = "COMMENTFILES")
    val COMMENTFILES: Boolean?,
    @Json(name = "DATA")
    val DATA: SiteStateDataDTO?,
    @Json(name = "GENERATION")
    val GENERATION: GENERATION_DTO?,
    @Json(name = "REGISTRACION_SMS")
    val REGISTRACION_SMS: String?,
    @Json(name = "SMSRASSILKA")
    val SMSRASSILKA: String?,
    @Json(name = "SMSPOLE")
    val SMSPOLE: Int?,
    @Json(name = "SOGLASHENIE")
    val SOGLASHENIE: SOGLASHENIE_DTO?,
    @Json(name = "SOGLASHENIETEXT")
    val SOGLASHENIE_TEXT: SOGLASHENIE_DTO?,
    @Json(name = "TESTSAITSSILKA")
    val TESTSAITSSILKA: String?,
    @Json(name = "CALL")
    val CALL: String?,
    @Json(name = "DANNYESSILKI")
    val DANNYESSILKI: AppLinksDTO?,
    @Json(name = "IDMAPKIT")
    val IDMAPKIT: MapKeysDTO?,
)

@Keep
data class MapKeysDTO(
    @Json(name = "ANDROID")
    val ANDROID: MapKeysValueDTO?,
)

@Keep
data class MapKeysValueDTO(
    @Json(name = "GEOKODER")
    val GEOKODER: String?,
    @Json(name = "MAPKIT")
    val MAPKIT: String?,
)

@Keep
data class AppLinksDTO(
    @Json(name = "POLITIKA")
    val politika: AppLinkDTO,
    @Json(name = "OFERTA")
    val oferta: AppLinkDTO,
    @Json(name = "PERDANNIE")
    val perdannie: AppLinkDTO,
    @Json(name = "DOSTAVAK")
    val dostavka: AppLinkDTO,
    @Json(name = "OPLATA")
    val oplata: AppLinkDTO,
)

@Keep
data class AppLinkDTO(
    @Json(name = "NAME")
    val name: String,
    @Json(name = "URL")
    val url: String,
)
