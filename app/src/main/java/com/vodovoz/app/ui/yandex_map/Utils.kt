package com.vodovoz.app.ui.yandex_map

import com.yandex.mapkit.Animation
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.Map

fun calculateBounds(
    p1: Point,
    p2: Point,
): BoundingBox {
    val south = minOf(p1.latitude, p2.latitude)
    val west = minOf(p1.longitude, p2.longitude)
    val north = maxOf(p1.latitude, p2.latitude)
    val east = maxOf(p1.longitude, p2.longitude)
    return BoundingBox(
        Point(north, east),
        Point(south, west)
    )
}

fun BoundingBox.pad(factor: Double): BoundingBox {
    val latSpan = northEast.latitude - southWest.latitude
    val lonSpan = northEast.longitude - southWest.longitude
    return BoundingBox(
        Point(northEast.latitude + latSpan * factor, northEast.longitude + lonSpan * factor),
        Point(southWest.latitude - latSpan * factor, southWest.longitude - lonSpan * factor)
    )
}


fun Map.plusZoom(animation: Animation = Animation(Animation.Type.LINEAR, 0.2f)) {
    val newZoom = cameraPosition.zoom + 1f
    move(
        CameraPosition(
            cameraPosition.target,
            newZoom,
            cameraPosition.azimuth,
            cameraPosition.tilt
        ),
        animation,
        null
    )
}

fun Map.minusZoom(animation: Animation = Animation(Animation.Type.LINEAR, 0.2f)) {
    val newZoom = cameraPosition.zoom - 1f
    move(
        CameraPosition(
            cameraPosition.target,
            newZoom,
            cameraPosition.azimuth,
            cameraPosition.tilt
        ),
        animation,
        null
    )
}


fun CameraPosition.copy(
    target: Point = this.target,
    zoom: Float = this.zoom,
    azimuth: Float = this.azimuth,
    tilt: Float = this.tilt
) = CameraPosition(target, zoom, azimuth, tilt)