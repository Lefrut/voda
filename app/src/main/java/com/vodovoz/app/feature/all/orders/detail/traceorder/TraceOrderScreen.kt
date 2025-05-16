package com.vodovoz.app.feature.all.orders.detail.traceorder

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.SheetValue.PartiallyExpanded
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.vodovoz.app.R
import com.vodovoz.app.design_system.composables.bottom_sheet.VodovozDragHandle
import com.vodovoz.app.design_system.composables.dialogs.VodovozDialog
import com.vodovoz.app.design_system.composables.top_bar.VodovozTopBar
import com.vodovoz.app.feature.all.orders.detail.traceorder.composables.TraceOrderBody
import com.vodovoz.app.feature.home.composables.dropShadow
import com.yandex.mapkit.mapview.MapView
import kotlin.math.roundToInt

@Composable
fun TraceOrderScreen(
    viewModel: TraceOrderViewModel,
    viewState: TraceOrderViewModel.TraceOrderState,
    mapView: () -> MapView,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        VodovozTopBar(
            onBack = { viewModel.navigateBack() },
            title = stringResource(R.string.where_is_my_order)
        )

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {

            TraceOrderBody(
                mapView = mapView,
                carPoint = viewState.carPoint,
                deliveryPoint = viewState.deliveryPoint,
                onGeoClick = {
                    viewModel.checkGeo()
                },
                onZoomPlus = {
                    viewModel.plusZoom()
                },
                onZoomMinus = {
                    viewModel.minusZoom()
                }
            )

            TraceOrderBottomSheet(
                modifier = Modifier
            )
        }
    }

    if (viewState.showSettingDialog) {
        VodovozDialog(
            title = stringResource(R.string.location_permission_title),
            description = stringResource(R.string.location_permission_description),
            acceptButtonText = stringResource(R.string.location_permission_accept),
            cancelButtonText = stringResource(R.string.location_permission_cancel),
            onDismiss = {
                viewModel.closeSettingsDialog()
            },
            onAccept = {
                viewModel.navigateToLocationSettings()
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TraceOrderBottomSheet(modifier: Modifier = Modifier) {
    val density = LocalDensity.current

    val partiallyExpandedDp = 262.dp
    val partiallyExpandedPx = with(density) { partiallyExpandedDp.toPx() }
    val hiddenPx = with(density) { 100.dp.toPx() }

    val state = remember {
        AnchoredDraggableState(
            initialValue = PartiallyExpanded,
            anchors = DraggableAnchors {
                Hidden at partiallyExpandedPx - hiddenPx
                PartiallyExpanded at 0f
            },
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(partiallyExpandedDp)
            .offset {
                IntOffset(
                    x = 0,
                    y = state
                        .requireOffset()
                        .roundToInt()
                )
            }
            .anchoredDraggable(
                state = state,
                orientation = Orientation.Vertical,
            )
            .dropShadow(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.onBackground.copy(0.14f),
                blur = 10.dp,
                offsetY = 8.dp
            )
            .dropShadow(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.onBackground.copy(0.12f),
                blur = 14.dp,
                offsetY = 3.dp
            )
            .dropShadow(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.onBackground.copy(0.2f),
                blur = 5.dp,
                offsetY = 5.dp
            )
            .background(MaterialTheme.colorScheme.background, MaterialTheme.shapes.medium)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VodovozDragHandle()
        Spacer(Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp, horizontal = 16.dp),
            verticalArrangement = Arrangement.Center
        ) {

        }
    }
}
