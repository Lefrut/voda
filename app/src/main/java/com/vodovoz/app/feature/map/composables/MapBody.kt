package com.vodovoz.app.feature.map.composables

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.SheetValue.PartiallyExpanded
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.vodovoz.app.R
import com.vodovoz.app.design_system.composables.bottom_sheet.VodovozDragHandle
import com.vodovoz.app.design_system.composables.button.VodovozButton
import com.vodovoz.app.design_system.composables.decoration.MapIconsColumn
import com.vodovoz.app.design_system.composables.decoration.SkeletonBox
import com.vodovoz.app.design_system.model.MapPointUi
import com.vodovoz.app.design_system.model.toMapPoint
import com.vodovoz.app.feature.home.composables.dropShadow
import com.vodovoz.app.ui.yandex_map.YandexMapUi
import com.yandex.mapkit.ScreenPoint
import com.yandex.mapkit.ScreenRect
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapBody(
    modifier: Modifier = Modifier,
    addressName: String,
    addressIsLoading: Boolean,
    yandexMap: YandexMapUi,
    anchoredDraggableState: AnchoredDraggableState<SheetValue>,
    onInputStart: () -> Unit,
    onInputEnd: () -> Unit,
    onZoomPlusClick: () -> Unit,
    onZoomMinusClick: () -> Unit,
    onGeoClick: () -> Unit,
    onCenterChanged: (MapPointUi?) -> Unit,
) {
    val density = LocalDensity.current

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val focusMapWidth = maxWidth
        val focusMapHeight = maxHeight - 220.dp
        val focusMapWidthPx = with(density) { focusMapWidth.toPx() }
        val focusMapHeightPx = with(density) { focusMapHeight.toPx() }

        Box(
            modifier = Modifier.pointerInput(focusMapWidth, focusMapHeight) {
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
                    val centerX = focusMapWidthPx.toBigDecimal().divide(two).toFloat()
                    val centerY = focusMapHeightPx.toBigDecimal().divide(two).toFloat()

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
                modifier = Modifier.fillMaxSize(),
                factory = { yandexMap.mapView }
            )
        }

        LaunchedEffect(Unit) {
            val mapView = yandexMap.mapView
            mapView.focusRect = ScreenRect(
                ScreenPoint(0f, 0f),
                ScreenPoint(focusMapWidthPx, focusMapHeightPx)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(focusMapHeight),
            contentAlignment = Alignment.Center
        ) {
            val markerHeight = 43.dp
            val markerWidth = 21.dp

            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_delivery),
                contentDescription = "Delivery Marker",
                modifier = Modifier
                    .width(markerWidth)
                    .height(markerHeight)
                    .offset(y = -(43.dp / 2)),
                contentScale = ContentScale.FillBounds
            )
        }

        DeliveryCard(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
        )

        MapIconsColumn(
            modifier = Modifier.align(Alignment.TopEnd),
            onZoomPlus = onZoomPlusClick,
            onZoomMinus = onZoomMinusClick,
            onGeoClick = onGeoClick
        )


        MapBottomSheet(
            modifier = Modifier.align(Alignment.BottomCenter),
            state = anchoredDraggableState,
            addressIsLoading = addressIsLoading,
            addressName = addressName
        )
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MapBottomSheet(
    modifier: Modifier = Modifier,
    addressName: String,
    addressIsLoading: Boolean,
    state: AnchoredDraggableState<SheetValue> = rememberSaveable(saver = AnchoredDraggableState.Saver()) {
        AnchoredDraggableState(initialValue = PartiallyExpanded)
    },
) {
    val density = LocalDensity.current

    val partiallyExpandedDp = 300.dp
    val partiallyExpandedPx = with(density) { partiallyExpandedDp.toPx() }


    LaunchedEffect(Unit) {
        state.updateAnchors(
            DraggableAnchors {
                Hidden at partiallyExpandedPx
                PartiallyExpanded at 0f
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = partiallyExpandedDp)
            .offset {
                val offsetY = runCatching {
                    state
                        .requireOffset()
                        .roundToInt()
                }.getOrNull() ?: 0
                IntOffset(
                    x = 0,
                    y = offsetY
                )
            }
            .anchoredDraggable(
                state = state,
                orientation = Orientation.Vertical,
            )
            .dropShadow(
                shape = MaterialTheme.shapes.medium,
                color = Color.Black.copy(0.4f),
                blur = 10.dp,
                offsetY = 4.dp
            )
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = MaterialTheme.shapes.medium.copy(
                    bottomEnd = CornerSize(0.dp),
                    bottomStart = CornerSize(0.dp)
                )
            ),
        horizontalAlignment = Alignment.Start
    ) {
        VodovozDragHandle(modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(id = R.string.delivery_address),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        val shimmer = rememberShimmer(ShimmerBounds.View)

        Row(
            modifier = Modifier
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .weight(weight = 1f, fill = false)
                .animateContentSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.icon_location),
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(24.dp),
                tint = MaterialTheme.colorScheme.secondary
            )

            when (addressIsLoading) {
                true -> {
                    SkeletonBox(
                        shimmerState = shimmer,
                        modifier = Modifier
                            .weight(1f)
                            .height(24.dp)
                    )
                }

                false -> {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = addressName,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            letterSpacing = 0.sp
                        ),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 3
                    )
                }
            }
        }

        VodovozButton(
            modifier = Modifier.padding(start = 16.dp, top = 20.dp, end = 16.dp, bottom = 16.dp),
            text = stringResource(id = R.string.bring_here_btn_text),
            onClick = {

            }
        )
    }

}