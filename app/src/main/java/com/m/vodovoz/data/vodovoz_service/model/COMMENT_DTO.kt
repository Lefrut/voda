package com.m.vodovoz.data.vodovoz_service.model


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class COMMENT_DTO(
    @Json(name = "DATA")
    val DATA: String?,
    @Json(name = "KYPLEN")
    val KYPLEN: String?,
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "RATING")
    val RATING: Int?,
    @Json(name = "TEXT")
    val TEXT: String?,
    @Json(name = "USER_PHOTO")
    val USER_PHOTO: String?,
    @Json(name = "IMAGES")
    val IMAGES: List<String>?,
    @Json(name = "IMAGESNEW")
    val MEDIA: List<CommentMediaDTO>?,
)