package com.m.vodovoz.data.maps.mappers

import com.m.vodovoz.data.maps.model.YandexGeoResponseDTO
import com.m.vodovoz.domain.general.model.location.MapAddressModel
import com.m.vodovoz.domain.general.model.location.MapPointModel
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.search.SuggestItem

fun YandexGeoResponseDTO.toDomain(): MapAddressModel? {

    val geoObject = info?.geoObjectCollection?.featureMember?.firstOrNull()?.geoObject

    val geo = geoObject?.point?.pos?.split(" ") ?: return null
    val address = geoObject.metaDataProperty?.geocoderMetaData?.address ?: return null
    val byKind = address.components?.associate { it.kind to it.name } ?: emptyMap()

    return MapAddressModel(
        point = MapPointModel(
            geo.last().toDouble(),
            geo.first().toDouble()
        ),
        name = address.formatted ?: "",
        city = byKind["locality"] ?: byKind["province"] ?: "",

        street = byKind["street"] ?: "",
        house = byKind["house"] ?: ""
    )
}


fun List<SuggestItem>.mapToDomain(): List<String> {
    return mapNotNull { suggestItem -> suggestItem.toDomain() }
}

fun SuggestItem.toDomain(): String? {
    if (uri?.contains("geo") == false) return null
    return displayText.toString().ifBlank { return null }
}

@JvmName("mapToMapPointModelList")
fun List<Point>.mapToDomain(): List<MapPointModel> {
    return mapNotNull { it.toDomain() }
}

fun Point.toDomain(): MapPointModel {
    return MapPointModel(latitude, longitude)
}

fun MapPointModel.toData(): Point {
    return Point(lat, lon)
}
