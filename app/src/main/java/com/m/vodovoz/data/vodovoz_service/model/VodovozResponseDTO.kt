package com.m.vodovoz.data.vodovoz_service.model

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class VodovozResponseDTO<T>(
    @Json(name = "status")
    val status: String?,
    @Json(name = "message")
    val message: String?,
    @Json(name = "data")
    val data: T?,
    @Json(name = "navigation")
    val navigation: VodovozNavigationDTO? = null,
)

val <T> VodovozResponseDTO<T>.messageOrEmpty: String
    get() = message ?: ""

@Keep
@JsonClass(generateAdapter = true)
data class VodovozNavigationDTO(
    @Json(name = "CURRENT_PAGE")
    val currentPage: Int?,
    @Json(name = "PAGE_SIZE")
    val pageSize: Int?,
    @Json(name = "TOTAL_COUNT")
    val totalCount: Int?,
    @Json(name = "TOTAL_PAGES")
    val totalPages: Int?,
)

@Keep
data class VodovozPlaceholderDTO(
    @Json(name = "TITLE") val title: String?,
    @Json(name = "ZAGALOVOK") val header: String?,
    @Json(name = "MESSAGE") val message: String?,
    @Json(name = "IMAGE") val imageUrl: String?,
    @Json(name = "KNOPKA") val button: VodovozButtonDTO?,
    @Json(name = "DELTOVAR") val productsSection: TOVARY_DTO?,
)

@Keep
data class VodovozButtonDTO(
    @Json(name = "TEXT")
    val text: String?,
    @Json(name = "COLOR")
    val color: String?,
    @Json(name = "BACKGROUND")
    val background: String?,
    @Json(name = "ID")
    val id: String?,
    @Json(name = "BRAYZER")
    val browser: String?,
    @Json(name = "URL")
    val url: String?
)
