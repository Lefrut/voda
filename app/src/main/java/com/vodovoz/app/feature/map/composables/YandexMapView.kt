package com.vodovoz.app.feature.map.composables

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.viewinterop.AndroidView
import com.vodovoz.app.design_system.model.MapPointUi
import com.vodovoz.app.design_system.model.toMapPoint
import com.vodovoz.app.ui.yandex_map.YandexMapUi
import com.yandex.mapkit.ScreenPoint
import com.yandex.mapkit.ScreenRect

@Composable
fun YandexMapView(
    modifier: Modifier = Modifier,
    yandexMap: YandexMapUi,
    focusMapHeightPx: Float,
    focusMapWidthPx: Float,
    onInputStart: () -> Unit,
    onInputEnd: () -> Unit,
    onCenterChanged: (MapPointUi?) -> Unit
) {
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
            factory = { yandexMap.mapView }
        )
    }

    LaunchedEffect(focusMapHeightPx, focusMapWidthPx) {
        val mapView = yandexMap.mapView
        mapView.focusRect = ScreenRect(
            ScreenPoint(0f, 0f),
            ScreenPoint(focusMapWidthPx, focusMapHeightPx)
        )
    }

}