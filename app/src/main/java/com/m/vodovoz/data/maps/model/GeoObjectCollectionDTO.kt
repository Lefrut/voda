package com.m.vodovoz.data.maps.model


import com.squareup.moshi.JsonClass
import androidx.annotation.Keep

@Keep
@JsonClass(generateAdapter = true)
data class GeoObjectCollectionDTO(
    val featureMember: List<FeatureMemberDTO>?
)