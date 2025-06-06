package com.vodovoz.app.data.vodovoz_service.model.address


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import androidx.annotation.Keep

@Keep
@JsonClass(generateAdapter = true)
data class ADDRESSES_SECTION_DTO(
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "DANNYE")
    val DANNYE: List<ADDRESS_ITEM_DTO>?
)