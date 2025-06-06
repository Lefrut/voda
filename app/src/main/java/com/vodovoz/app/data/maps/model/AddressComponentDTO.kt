package com.vodovoz.app.data.maps.model


import com.squareup.moshi.JsonClass
import androidx.annotation.Keep

@Keep
@JsonClass(generateAdapter = true)
data class AddressComponentDTO(
    val kind: String?,
    val name: String?
)