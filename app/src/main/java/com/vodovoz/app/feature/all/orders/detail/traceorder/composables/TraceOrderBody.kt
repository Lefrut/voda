package com.vodovoz.app.feature.all.orders.detail.traceorder.composables

import android.annotation.SuppressLint
import android.graphics.PointF
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.animateValueAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.vodovoz.app.R
import com.vodovoz.app.design_system.composables.decoration.MapIconsColumn
import com.vodovoz.app.design_system.model.MapPointUi
import com.vodovoz.app.ui.yandex_map.YandexMapUi
import com.vodovoz.app.util.extensions.getBitmap
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.runtime.image.ImageProvider

@SuppressLint("ClickableViewAccessibility")
@Composable
fun TraceOrderBody(
    modifier: Modifier = Modifier,
    yandexMap: YandexMapUi,
    deliveryPoint: MapPointUi?,
    carPoint: MapPointUi?,
    onZoomPlus: () -> Unit,
    onZoomMinus: () -> Unit,
    onGeoClick: () -> Unit,
    onDragStop: () -> Unit,
    onDragStart: () -> Unit,
) {
    val context = LocalContext.current

    val deliveryImageProvider = remember {
        ImageProvider.fromBitmap(context.getBitmap(R.drawable.ic_delivery))
    }

    val carImageProvider = remember {
        ImageProvider.fromBitmap(context.getBitmap(R.drawable.ic_car))
    }

    val animatablePoint = remember { Animatable(null, MapPointUi.VectorConverter) }

    LaunchedEffect(carPoint) {
        if (carPoint == null) return@LaunchedEffect

        if (animatablePoint.value == null) {
            animatablePoint.snapTo(carPoint)
        } else {
            animatablePoint.animateTo(
                targetValue = carPoint,
                animationSpec = tween(durationMillis = 5000, easing = LinearEasing)
            )
        }
    }

    val carPlacemark = remember { mutableStateOf<PlacemarkMapObject?>(null) }
    val deliveryPlacemark = remember { mutableStateOf<PlacemarkMapObject?>(null) }


    Box(
        modifier = modifier.fillMaxSize()
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(pass = PointerEventPass.Initial)
                        onDragStart()
                        waitForUpOrCancellation(pass = PointerEventPass.Initial)
                        onDragStop()
                    }
                },
            factory = {
                val view = yandexMap.mapView
                view
            },
            update = { view ->
                val map = view.mapWindow.map
                val mapObjects = map.mapObjects

                if (deliveryPlacemark.value == null && deliveryPoint != null) {
                    deliveryPlacemark.value =
                        mapObjects.addPlacemark(Point(deliveryPoint.lat, deliveryPoint.lon)).apply {
                            setIcon(deliveryImageProvider)
                            setIconStyle(
                                IconStyle().apply {
                                    anchor = PointF(0.5f, 1.0f)
                                    scale = 1f
                                    zIndex = 10f
                                }
                            )
                        }
                }

                val animatedCartPoint = animatablePoint.value
                if (animatedCartPoint != null) {
                    val current = carPlacemark.value
                    if (current == null) {
                        carPlacemark.value = mapObjects.addPlacemark(Point(animatedCartPoint.lat, animatedCartPoint.lon)).apply {
                            setIcon(carImageProvider)
                            setIconStyle(
                                IconStyle().apply {
                                    anchor = PointF(0.5f, 1.0f)
                                    scale = 1f
                                    zIndex = 5f
                                }
                            )
                        }
                    } else {
                        current.geometry = Point(
                            animatedCartPoint.lat,
                            animatedCartPoint.lon
                        )
                    }
                }
            }
        )

        MapIconsColumn(
            modifier = Modifier.align(Alignment.TopEnd),
            onZoomPlus = onZoomPlus,
            onZoomMinus = onZoomMinus,
            onGeoClick = onGeoClick
        )
    }

}