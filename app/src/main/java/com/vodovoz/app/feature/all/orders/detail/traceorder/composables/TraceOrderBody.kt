package com.vodovoz.app.feature.all.orders.detail.traceorder.composables

import android.annotation.SuppressLint
import android.graphics.PointF
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.animateValueAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.vodovoz.app.R
import com.vodovoz.app.design_system.model.MapPointUi
import com.vodovoz.app.feature.home.composables.dropShadow
import com.vodovoz.app.util.extensions.getBitmap
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider

@SuppressLint("ClickableViewAccessibility")
@Composable
fun TraceOrderBody(
    modifier: Modifier = Modifier,
    mapView: () -> MapView,
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
                point?.lat?.toFloat() ?: 0f
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
                val view = mapView()
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

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 20.dp)
        ) {
            MapIcon(
                painter = painterResource(id = R.drawable.ic_plus_white),
                onClick = onZoomPlus
            )

            MapIcon(
                modifier = Modifier.padding(top = 8.dp),
                painter = painterResource(id = R.drawable.ic_minus_blue),
                onClick = onZoomMinus
            )

            MapIcon(
                modifier = Modifier.padding(top = 32.dp),
                painter = painterResource(id = R.drawable.ic_geo_location),
                tint = MaterialTheme.colorScheme.primary,
                onClick = onGeoClick
            )

        }
    }

}

@Composable
private fun MapIcon(
    modifier: Modifier = Modifier,
    painter: Painter,
    tint: Color = MaterialTheme.colorScheme.onBackground,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .dropShadow(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.onBackground,
                blur = 2.dp,
                offsetY = 1.dp,
                offsetX = 0.dp
            )
            .background(MaterialTheme.colorScheme.background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painter,
            contentDescription = null,
            tint = tint
        )
    }
}