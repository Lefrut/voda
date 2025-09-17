package com.m.vodovoz.data.vodovoz_service.model.filters

import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
data class FiltersDTO(
    @Json(name = "CENAFILTER")
    val CENAFILTER: FilterBoundsDTO? = null,
    @Json(name = "DANNIE")
    val DANNIE: List<FilterDTO>? = null
)

@Keep
data class FilterBoundsDTO(
    @Json(name = "MIN")
    val MIN: String? = null,
    @Json(name = "MAX")
    val MAX: String? = null
)

@Keep
data class FilterDTO(
    @Json(name = "NAME")
    val NAME: String? = null,

    @Json(name = "CODE")
    val CODE: String? = null,

    @Json(name = "TYPE")
    val TYPE: String? = null,

    @Json(name = "ZNACHEIE")
    val ZNACHEIE: FilterValuesDTO?,

    @Json(name = "ZHACFILTER")
    val ZHACFILTER: FilterBoundsDTO?
)

@Keep
data class FilterValuesDTO(
    @Json(name = "COUNT")
    val COUNT: Int?,
    @Json(name = "DATA")
    val DATA: List<String>?
)
