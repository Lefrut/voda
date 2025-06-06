package com.vodovoz.app.design_system.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.domain.general.model.MapPointModel
import com.yandex.mapkit.geometry.Point

@Immutable
data class MapPointUi(
    val lat: Double,
    val lon: Double,
) {
    companion object {
        val Empty = MapPointUi(0.0, 0.0)
    }
}

fun Point.toMapPoint(): MapPointUi {
    return MapPointUi(lat = latitude, lon = longitude)
}

fun MapPointModel.toUi(): MapPointUi {
    return MapPointUi(lat, lon)
}

fun MapPointUi.toPoint(): Point {
    return Point(lat, lon)
}
