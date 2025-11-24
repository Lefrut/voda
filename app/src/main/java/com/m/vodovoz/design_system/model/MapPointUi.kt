package com.m.vodovoz.design_system.model

import android.os.Parcelable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.runtime.Immutable
import com.m.vodovoz.domain.general.model.location.MapPointModel
import com.m.vodovoz.feature.map.model.MapAreaUi
import com.m.vodovoz.ui.yandex_map.distanceBetween
import com.yandex.mapkit.geometry.Point
import kotlinx.parcelize.Parcelize

@Parcelize
@Immutable
data class MapPointUi(
    val lat: Double,
    val lon: Double,
) : Parcelable {
    companion object {
        val Empty = MapPointUi(0.0, 0.0)

        val VectorConverter = TwoWayConverter<MapPointUi?, AnimationVector2D>(
            convertToVector = { point ->
                AnimationVector2D(point?.lat?.toFloat() ?: 0f, point?.lon?.toFloat() ?: 0f)
            },
            convertFromVector = { vector ->
                MapPointUi(vector.v1.toDouble(), vector.v2.toDouble())
            },
        )
    }
}

fun List<MapPointUi>.distanceKm(): Float {
    return zipWithNext { a, b ->
        a.distanceBetween(b).toFloat()
    }.sum() / 1000f
}


fun MapAreaUi.contains(point: MapPointUi): Boolean {
    val x = point.lon
    val y = point.lat
    var inside = false
    val pts = this.points
    val n = pts.size
    for (i in 0 until n) {
        val j = (i + n - 1) % n
        val xi = pts[i].lon
        val yi = pts[i].lat
        val xj = pts[j].lon
        val yj = pts[j].lat
        val intersect = ((yi > y) != (yj > y)) &&
                (x < (xj - xi) * (y - yi) / (yj - yi) + xi)
        if (intersect) inside = !inside
    }
    return inside
}

fun Point.toMapPoint(): MapPointUi {
    return MapPointUi(lat = latitude, lon = longitude)
}

fun MapPointModel.toUi(): MapPointUi {
    return MapPointUi(lat, lon)
}

fun List<MapPointModel>.mapToUi(): List<MapPointUi> {
    return map { point -> point.toUi() }
}

fun MapPointUi.toDomain(): MapPointModel {
    return MapPointModel(lat, lon)
}


fun MapPointUi.toPoint(): Point {
    return Point(lat, lon)
}

fun List<MapPointUi>.mapToPoints(): List<Point> {
    return map { Point(it.lat, it.lon) }
}
