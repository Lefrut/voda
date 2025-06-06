package com.vodovoz.app.feature.all.orders.detail.traceorder.composables

import android.annotation.SuppressLint
import android.graphics.PointF
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.animateValueAsState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.vodovoz.app.R
import com.vodovoz.app.design_system.composables.decoration.MapIcon
import com.vodovoz.app.design_system.composables.decoration.MapIconsColumn
import com.vodovoz.app.design_system.model.MapPointUi
import com.vodovoz.app.ui.yandex_map.YandexMapUi
import com.vodovoz.app.util.extensions.getBitmap
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.IconStyle
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

    val animatedCartPoint by animateValueAsState(
        targetValue = carPoint,
        typeConverter = TwoWayConverter(
            convertToVector = { point ->
                AnimationVector2D(
                    point?.lat?.toFloat() ?: 0f,
                    point?.lon?.toFloat() ?: 0f
                )
            },
            convertFromVector = { vector ->
                MapPointUi(vector.v1.toDouble(), vector.v2.toDouble())
            }
        ),
        label = "animatedCarLat"
    )




    Box(
        modifier = modifier
            .fillMaxSize()
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

                mapObjects.clear()

                deliveryPoint?.let {
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

                animatedCartPoint?.let { point ->
                    mapObjects.addPlacemark(Point(point.lat, point.lon)).apply {
                        setIcon(carImageProvider)
                        setIconStyle(
                            IconStyle().apply {
                                anchor = PointF(0.5f, 1.0f)
                                scale = 1f
                                zIndex = 5f
                            }
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