package com.m.vodovoz.data.vodovoz_service.model


import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.m.vodovoz.data.vodovoz_service.model.auth.KNOPKA_AUTH_DTO

@Keep
data class TOVAR_DATA_DTO(
    @Json(name = "CATALOG_QUANTITY")
    val CATALOG_QUANTITY: Int?,
    @Json(name = "DETAIL_PICTURE")
    val DETAIL_PICTURE: String?,
    @Json(name = "EDINICAIZMERENIYA")
    val EDINICAIZMERENIYA: String?,
    @Json(name = "EXTENDED_PRICE")
    val EXTENDED_PRICE: List<EXTENDED_PRICE_DTO?>?,
    @Json(name = "FAVORITE")
    val FAVORITE: Boolean?,
    @Json(name = "ID")
    val ID: Long?,
    @Json(name = "KOFFICIENT")
    val KOFFICIENT: Int?,
    @Json(name = "NALICHIE_MORE")
    val NALICHIE_MORE: List<NALICHIE_MORE_DTO>?,
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "PROPERTY_RATING_VALUE")
    val PROPERTY_RATING_VALUE: Float?,
    @Json(name = "PROPERTY_TSENA_ZA_EDINITSU_TOVARA_VALUE")
    val PROPERTY_TSENA_ZA_EDINITSU_TOVARA_VALUE: String?,
    @Json(name = "PROPERTY_ZALOG_VALUE")
    val PROPERTY_ZALOG_VALUE: Int?,
    @Json(name = "TOVAR18")
    val TOVAR18: TOVAR_18_DTO?,
    @Json(name = "KNOPKI")
    val KNOPKI: KNOPKA_AUTH_DTO?,
)