package com.vodovoz.app.data.vodovoz_service.model.filters

import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class FiltersDTO(
    @Expose
    @Json(name = "CENAFILTER")
    val CENAFILTER: FilterBoundsDTO? = null,

    @Expose
    @Json(name = "DANNIE")
    val DANNIE: List<FilterDTO>? = null
)

@Keep
data class FilterBoundsDTO(
    @Expose
    @Json(name = "MIN")
    val MIN: String? = null,

    @Expose
    @Json(name = "MAX")
    val MAX: String? = null
)

@Keep
data class FilterDTO(
    @Expose
    @Json(name = "NAME")
    val NAME: String? = null,

    @Expose
    @Json(name = "CODE")
    val CODE: String? = null,

    @Expose
    @Json(name = "TYPE")
    val TYPE: String? = null,

    @Expose
    @Json(name = "ZNACHEIE")
    val ZNACHEIE: FilterValuesDTO?,

    @Json(name = "ZHACFILTER")
    val ZHACFILTER: FilterBoundsDTO?
)

@Keep
data class FilterValuesDTO(
    @Expose
    @Json(name = "COUNT")
    val COUNT: Int?,
    @Expose
    @Json(name = "DATA")
    val DATA: List<String>?
)
