package com.vodovoz.app.data.vodovoz_service.model

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
data class AnalogsSectionDTO(
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "SORTIROVKA")
    val SORTIROVKA: SORTIROVKA_DTO?,
    @Json(name = "TOVAR")
    val TOVAR: List<TOVAR_DATA_DTO>?,
    @Json(name = "TOVAR18")
    val TOVAR18: TOVAR_18_DTO?
)

@Keep
@JsonClass(generateAdapter = true)
data class ProductsSectionDTO(
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "COUNT")
    val COUNT: String?,
    @Json(name = "TOVAR18")
    val TOVAR18: TOVAR_18_DTO?,
    @Json(name = "TOVARVSEGO")
    val TOVARVSEGO: String?,
    @Json(name = "STRANIC")
    val STRANIC: Int?,
    @Json(name = "SORTIROVKA")
    val SORTIROVKA: SORTIROVKA_DTO?,
    @Json(name = "RAZDEL")
    val RAZDEL: CategoriesDTO?,
    @Json(name = "DATA")
    val DATA: List<TOVAR_DATA_DTO>?,
    @Json(name = "TOVAR")
    val TOVAR: List<TOVAR_DATA_DTO>?,
    @Json(name = "SHARE")
    val SHARE: PODELITCA_DTO?,
)

@Keep
class CategoriesDTO(
    @Json(name = "LISTRAZDEL")
    val LISTRAZDEL: List<CATEGORY_DTO?>?,
    @Json(name = "TITLERAZDEL")
    val TITLERAZDEL: String?,
)

@Keep
class PODELITCA_DTO(
    @Json(name = "detail_page_url")
    val detailPageUrl: String? = null,
    @Json(name = "detail_page_url_ios")
    val detailPageUrlIOS: PAGE_URL_IOS_DTO? = null,
)

@Keep
class PAGE_URL_IOS_DTO(
    @Json(name = "NAME")
    val name: String? = null,
    @Json(name = "URL")
    val url: String? = null,
)