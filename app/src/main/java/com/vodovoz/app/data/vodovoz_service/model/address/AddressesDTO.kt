package com.vodovoz.app.data.vodovoz_service.model.address


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import androidx.annotation.Keep

@Keep
@JsonClass(generateAdapter = true)
data class AddressesDTO(
    @Json(name = "FIZLICO")
    val FIZLICO: ADDRESSES_SECTION_DTO?,
    @Json(name = "YRLICO")
    val YRLICO: ADDRESSES_SECTION_DTO?
)