package com.vodovoz.app.data.vodovoz_service.model.address


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import androidx.annotation.Keep

@Keep
@JsonClass(generateAdapter = true)
data class ADDRESS_ITEM_DTO(
    @Json(name = "ID")
    val ID: Long?,
    @Json(name = "PERSON_TYPE_ID")
    val PERSON_TYPE_ID: Int?,
    @Json(name = "OPISANIE")
    val OPISANIE: String?,
    @Json(name = "ADRESS")
    val ADRESS: String?
)