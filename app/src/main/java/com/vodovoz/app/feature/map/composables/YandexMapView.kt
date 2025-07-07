package com.vodovoz.app.feature.map.composables

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.viewinterop.AndroidView
import com.vodovoz.app.design_system.model.MapPointUi
import com.vodovoz.app.design_system.model.toMapPoint
import com.vodovoz.app.design_system.model.toPoint
import com.vodovoz.app.feature.map.model.MapAreaUi
import com.vodovoz.app.ui.yandex_map.YandexMapUi
import com.yandex.mapkit.ScreenPoint
import com.yandex.mapkit.ScreenRect
import com.yandex.mapkit.geometry.LinearRing
import com.yandex.mapkit.geometry.Polygon
import com.yandex.mapkit.map.CameraListener

@Suppress("NonSkippableComposable")
@Composable
fun YandexMapView(
    modifier: Modifier = Modifier,
    yandexMap: YandexMapUi,
    areas: List<MapAreaUi>,
    focusMapHeightPx: Float,
    focusMapWidthPx: Float,
    onInputStart: () -> Unit,
    onInputEnd: () -> Unit,
    onCenterChanged: (MapPointUi?) -> Unit,
) {
    LaunchedEffect(focusMapHeightPx, focusMapWidthPx) {
        val mapView = yandexMap.mapView
        mapView.focusRect = ScreenRect(
            ScreenPoint(0f, 0f),
            ScreenPoint(focusMapWidthPx, focusMapHeightPx)
        )
    }


    val polygonMapObjects by remember(areas) {
        mutableStateOf(areas.map { area ->
            val points = area.points.map { point -> point.toPoint() }

            val polygon = Polygon(LinearRing(points), listOf())

            yandexMap.mapView.map.mapObjects.addPolygon(polygon).apply {
                fillColor = area.color.copy(0.15f).toArgb()
                strokeColor = area.color.copy(0.45f).toArgb()
                strokeWidth = 2f
                isVisible = false
            }
        }
        )
    }

    DisposableEffect(key1 = polygonMapObjects) {
        val mapView = yandexMap.mapView
        val map = mapView.map
        val cameraListener = CameraListener { _, cameraPosition, _, _ ->
            val showPolygons = cameraPosition.zoom < 12f
            polygonMapObjects.forEach { mapObject ->
                mapObject.isVisible = showPolygons
            }
        }
        map.addCameraListener(cameraListener)

        onDispose {
            map.removeCameraListener(cameraListener)
        }

    }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                var interactionInProgress = false

                awaitEachGesture {
                    if (interactionInProgress) return@awaitEachGesture

                    interactionInProgress = true

                    awaitFirstDown(pass = PointerEventPass.Initial)

                    onInputStart()
                    do {
                        val event = awaitPointerEvent()
                    } while (event.changes.any { change -> change.pressed })
                    onInputEnd()

                    val two = 2f.toBigDecimal()
                    val centerX = focusMapWidthPx
                        .toBigDecimal()
                        .divide(two)
                        .toFloat()
                    val centerY = focusMapHeightPx
                        .toBigDecimal()
                        .divide(two)
                        .toFloat()

                    val screenPoint = ScreenPoint(centerX, centerY)

                    val point = yandexMap.mapView.mapWindow
                        .screenToWorld(screenPoint)
                        ?.toMapPoint()

                    onCenterChanged(point)

                    interactionInProgress = false
                }
            }
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = {
                yandexMap.mapView
            }
        )
    }
}