package com.vodovoz.app.feature.map

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.SheetValue.PartiallyExpanded
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.vodovoz.app.R
import com.vodovoz.app.design_system.composables.bottom_sheet.VodovozDragHandle
import com.vodovoz.app.design_system.composables.decoration.MapIconsColumn
import com.vodovoz.app.design_system.model.MapPointUi
import com.vodovoz.app.design_system.model.toMapPoint
import com.vodovoz.app.feature.home.composables.dropShadow
import com.vodovoz.app.feature.map.composables.DeliveryCard
import com.vodovoz.app.feature.map.composables.MapTopBar
import com.vodovoz.app.ui.yandex_map.YandexMapUi
import com.vodovoz.app.util.extensions.getBitmap
import com.yandex.mapkit.ScreenPoint
import com.yandex.mapkit.ScreenRect
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapFlowViewModel,
    viewState: MapFlowViewModel.MapFlowState,
    yandexMap: YandexMapUi,
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    var targetOffset by remember { mutableStateOf(0.dp) }

    val animatedOffset by animateDpAsState(
        targetValue = targetOffset,
        animationSpec = spring(stiffness = Spring.StiffnessMedium), label = ""
    )

    var markerGeoPoint by remember { mutableStateOf<MapPointUi?>(null) }

    val deliveryImageBitmap = remember {
        context.getBitmap(R.drawable.ic_delivery).asImageBitmap()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        MapTopBar(
            query = viewState.query,
            onQueryChange = viewModel::changeQuery,
            onSearchClick = viewModel::searchAddressByQuery
        )
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            var mapBottomSheetHeightPx by rememberSaveable {
                mutableFloatStateOf(0f)
            }

            val focusMapWidth = maxWidth
            val focusMapHeight = maxHeight - with(density) { mapBottomSheetHeightPx.toDp() }

            val focusMapWidthPx = with(density) { focusMapWidth.toPx() }
            val focusMapHeightPx = with(density) { focusMapHeight.toPx() }

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    val mapView = yandexMap.mapView

                    mapView.map.addCameraListener { _, _, _, finished ->
                        targetOffset = if (!finished) (-12).dp else 0.dp

                        val centerX = focusMapWidthPx / 2f
                        val centerY = focusMapHeightPx / 2f

                        val screenPoint = ScreenPoint(
                            centerX, centerY + with(density) { targetOffset.toPx() }
                        )
                        markerGeoPoint = mapView.mapWindow.screenToWorld(screenPoint)?.toMapPoint()
                    }

                    mapView
                }
            )

            LaunchedEffect(focusMapHeightPx) {
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
                Image(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_delivery),
                    contentDescription = "Delivery Marker",
                    modifier = Modifier
                        .width(21.dp)
                        .height(43.dp)
                        .offset {
                            IntOffset(x = 0, y = animatedOffset.roundToPx())
                        },
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
                onZoomPlus = {
                    viewModel.plusZoom()
                },
                onZoomMinus = {
                    viewModel.minusZoom()
                },
                onGeoClick = {
                    viewModel.checkGeo()
                }
            )


            MapBottomSheet(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .onSizeChanged { size ->
                        mapBottomSheetHeightPx = size.height.toFloat()
                    }
            )


        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MapBottomSheet(
    modifier: Modifier = Modifier,
    state: AnchoredDraggableState<SheetValue> = remember {
        AnchoredDraggableState(initialValue = PartiallyExpanded)
    },
) {
    val density = LocalDensity.current

    val partiallyExpandedDp = 220.dp
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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VodovozDragHandle()
        Spacer(Modifier.height(20.dp))
        Box(modifier = Modifier.size(700.dp))
    }

}