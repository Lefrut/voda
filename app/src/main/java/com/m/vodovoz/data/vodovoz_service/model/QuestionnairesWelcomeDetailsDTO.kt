package com.m.vodovoz.data.vodovoz_service.model


import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.m.vodovoz.data.vodovoz_service.model.auth.KNOPKA_AUTH_DTO

@Keep
data class QuestionnairesWelcomeDetailsDTO(
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "KARTINKA")
    val KARTINKA: String?,
    @Json(name = "ZAGOLOVOK")
    val ZAGOLOVOK: String?,
    @Json(name = "OPISANIE")
    val OPISANIE: String?,
    @Json(name = "KNOPKA")
    val KNOPKA: List<KNOPKA_AUTH_DTO>?
)