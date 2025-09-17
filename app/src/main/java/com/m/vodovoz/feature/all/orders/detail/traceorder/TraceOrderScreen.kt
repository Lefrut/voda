package com.m.vodovoz.feature.all.orders.detail.traceorder

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.SheetValue.PartiallyExpanded
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.m.vodovoz.R
import com.m.vodovoz.design_system.composables.bottom_sheet.VodovozDragHandle
import com.m.vodovoz.design_system.composables.button.VodovozButtonDefaults
import com.m.vodovoz.design_system.composables.button.VodovozButtonSmall
import com.m.vodovoz.design_system.composables.dialogs.VodovozDialog
import com.m.vodovoz.design_system.composables.top_bar.VodovozTopBar
import com.m.vodovoz.design_system.model.ImageAndTextUi
import com.m.vodovoz.design_system.model.ImageButtonUi
import com.m.vodovoz.feature.all.orders.detail.traceorder.composables.TraceOrderBody
import com.m.vodovoz.feature.home.composables.dropShadow
import com.m.vodovoz.ui.yandex_map.YandexMapUi
import com.yandex.mapkit.ScreenPoint
import com.yandex.mapkit.ScreenRect
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TraceOrderScreen(
    viewModel: TraceOrderViewModel,
    viewState: TraceOrderViewModel.TraceOrderState,
    anchoredDraggableState: AnchoredDraggableState<SheetValue>,
    yandexMap: YandexMapUi,
) {
    val density = LocalDensity.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        VodovozTopBar(
            onBack = { viewModel.navigateBack() },
            title = stringResource(R.string.where_is_my_order)
        )

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {

            TraceOrderBody(
                yandexMap = yandexMap,
                carPoint = viewState.carPoint,
                deliveryPoint = viewState.finishPoint,
                onGeoClick = {
                    viewModel.moveToAvailableGeo()
                },
                onZoomPlus = {
                    viewModel.plusZoom()
                },
                onZoomMinus = {
                    viewModel.minusZoom()
                },
                onDragStart = {
                    viewModel.hideBottomSheet()
                },
                onDragStop = {
                    viewModel.showBottomSheet()
                }
            )

            var sheetHeightPx by remember { mutableIntStateOf(0) }

            TraceOrderBottomSheet(
                modifier = Modifier.onSizeChanged { size ->
                    sheetHeightPx = size.height
                },
                state = anchoredDraggableState,
                title = viewState.bottomSheetTitle,
                description = viewState.bottomSheetDescription,
                buttons = viewState.bottomSheetButtons,
                items = viewState.bottomSheetItems,
                onButtonClick = { imageButton ->
                    viewModel.activateButton(imageButton)
                }
            )


            LaunchedEffect(sheetHeightPx) {
                val screenWidthPx = with(density) { maxWidth.toPx() }
                val screenHeightPx = with(density) { maxHeight.toPx() - sheetHeightPx }

                yandexMap.mapView.focusRect = ScreenRect(
                    ScreenPoint(0f, 0f),
                    ScreenPoint(
                        screenWidthPx,
                        screenHeightPx
                    )
                )
            }
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


@Suppress("NonSkippableComposable")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TraceOrderBottomSheet(
    modifier: Modifier = Modifier,
    state: AnchoredDraggableState<SheetValue>,
    title: String,
    description: String,
    buttons: List<ImageButtonUi>,
    items: List<ImageAndTextUi>,
    onButtonClick: (ImageButtonUi) -> Unit,
) {
    val density = LocalDensity.current

    val partiallyExpandedDp = 262.dp
    val partiallyExpandedPx = with(density) { partiallyExpandedDp.toPx() }
    val hiddenPx = partiallyExpandedPx / 2


    LaunchedEffect(Unit) {
        state.updateAnchors(
            DraggableAnchors {
                Hidden at partiallyExpandedPx - hiddenPx
                PartiallyExpanded at 0f
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = partiallyExpandedDp + partiallyExpandedDp / 2)
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
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = MaterialTheme.shapes.medium.copy(
                    bottomEnd = CornerSize(0.dp),
                    bottomStart = CornerSize(0.dp)
                )
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (title.isNotEmpty() || buttons.isNotEmpty() || items.isNotEmpty() || description.isNotEmpty()) {
            VodovozDragHandle()
            Spacer(Modifier.height(20.dp))
        }


        if (title.isNotEmpty()) {
            Text(
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp),
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall
            )
        }
        if (description.isNotEmpty()) {
            Text(
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp),
                text = description,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (items.isNotEmpty()) {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                items.forEach { item ->
                    TraceOrderBottomSheetItem(
                        image = item.image,
                        name = item.text
                    )
                }
            }
        }

        if (buttons.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 2,
            ) {
                buttons.forEach { button ->
                    VodovozButtonSmall(
                        modifier = Modifier.weight(1f),
                        onClick = { onButtonClick(button) },
                        colors = VodovozButtonDefaults.secondaryColors().copy(
                            containerColor = button.containerColor,
                            contentColor = button.contentColor
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = button.image,
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .size(18.dp),
                                contentScale = ContentScale.FillBounds
                            )

                            Text(
                                text = button.name,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    letterSpacing = 0.1.sp
                                ),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun TraceOrderBottomSheetItem(modifier: Modifier = Modifier, image: String, name: String) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = image,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            contentScale = ContentScale.FillBounds
        )

        if (image.isNotEmpty()) {
            Spacer(modifier = Modifier.width(24.dp))
        }

        Text(
            modifier = Modifier.weight(1f),
            text = name,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium
        )

    }
}
