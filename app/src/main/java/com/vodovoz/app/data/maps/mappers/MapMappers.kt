package com.vodovoz.app.data.maps.mappers

import com.vodovoz.app.data.maps.model.YandexGeoResponseDTO
import com.vodovoz.app.domain.general.model.location.MapPointModel
import com.vodovoz.app.domain.general.model.location.MapAddressModel

fun YandexGeoResponseDTO.toDomain(): MapAddressModel? {

    val geoObject = info?.geoObjectCollection?.featureMember?.firstOrNull()?.geoObject

    val geo = geoObject?.point?.pos?.split(" ") ?: return null
    val address = geoObject.metaDataProperty?.geocoderMetaData?.address ?: return null
    val byKind = address.components?.associate { it.kind to it.name } ?: emptyMap()

    return MapAddressModel(
        point = MapPointModel(
            geo.first().toDouble(),
            geo.last().toDouble()
        ),
        name = address.formatted ?: "",
        city = byKind["locality"] ?: byKind["province"] ?: "",

        street = byKind["street"] ?: "",
        house = byKind["house"] ?: ""
    )
}