package com.vodovoz.app.feature.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vodovoz.app.design_system.modifiers.lifecycleWindowInsets
import com.vodovoz.app.feature.map.composables.MapBody
import com.vodovoz.app.feature.map.composables.MapSearchList
import com.vodovoz.app.feature.map.composables.MapTopBar
import com.vodovoz.app.ui.yandex_map.YandexMapUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapFlowViewModel,
    viewState: MapFlowViewModel.MapFlowState,
    yandexMap: YandexMapUi,
    anchoredDraggableState: AnchoredDraggableState<SheetValue>,
) {


    Column(
        modifier = Modifier
            .lifecycleWindowInsets(WindowInsets.statusBars)
            .fillMaxSize(),
    ) {
        MapTopBar(
            query = viewState.query,
            onQueryChange = viewModel::changeQuery,
            onSearchClick = viewModel::searchAddressByQuery,
            onFieldClick = {
                viewModel.changeToSearchMode()
            },
            onBackClick = {
                viewModel.navigateBack()
            }
        )

        Box {
            MapBody(
                anchoredDraggableState = anchoredDraggableState,
                addressName = viewState.address?.name ?: "",
                addressIsLoading = viewState.addressIsLoading,
                yandexMap = yandexMap,
                onInputStart = {
                    viewModel.hideAddressBottomSheet()
                },
                onInputEnd = {
                    viewModel.showAddressBottomSheet()
                },
                onGeoClick = {
                    viewModel.moveToUserGeo()
                },
                onCenterChanged = { point ->
                    viewModel.changeMarkerPoint(point)
                },
                onZoomPlusClick = {
                    viewModel.plusZoom()
                },
                onZoomMinusClick = {
                    viewModel.minusZoom()
                }
            )

            Column(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(
                    visible = viewState.mode == MapFlowViewModel.MapUiMode.Search,
                    enter = slideInVertically(
                        initialOffsetY = { fullHeight -> -fullHeight },
                        animationSpec = tween(durationMillis = 150, easing = LinearOutSlowInEasing)
                    ) + fadeIn(),
                    exit = slideOutVertically(
                        targetOffsetY = { fullHeight -> -fullHeight },
                        animationSpec = tween(durationMillis = 250, easing = FastOutLinearInEasing)
                    ) + fadeOut()
                ) {
                    MapSearchList()
                }
            }
        }

    }
}


