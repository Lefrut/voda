package com.vodovoz.app.feature.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.vodovoz.app.R
import com.vodovoz.app.design_system.composables.dialogs.VodovozDialog
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

    if (viewState.showSettingsDialog) {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
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
            },
            onClearClick = {
                viewModel.changeQuery("")
            }
        )

        Box {
            MapBody(
                anchoredDraggableState = anchoredDraggableState,
                addressName = viewState.currentAddress?.name ?: "",
                addressIsLoading = viewState.addressIsLoading,
                addressIsError = viewState.addressIsError,
                screenType = viewState.screenType,
                yandexMap = yandexMap,
                buttonIsLoading = viewState.buttonIsLoading,
                onInputStart = {
                    viewModel.hideAddressBottomSheet()
                },
                onInputEnd = {
                    viewModel.showAddressBottomSheet()
                },
                onGeoClick = {
                    viewModel.checkGeo()
                },
                onCenterChanged = { point ->
                    viewModel.searchAddress(point)
                },
                onZoomPlusClick = {
                    viewModel.plusZoom()
                },
                onZoomMinusClick = {
                    viewModel.minusZoom()
                },
                onBottomSheetButtonClick = {
                    viewModel.navigateToAddAddress()
                }
            )

            Column(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(
                    modifier = Modifier.fillMaxSize(),
                    visible = viewState.mode == MapFlowViewModel.MapUiMode.Search && viewState.query.isNotBlank() && viewState.recommendedAddresses.isNotEmpty(),
                    enter = fadeIn(tween(150)),
                    exit = fadeOut(tween(120))
                ) {
                    MapSearchList(
                        recommendedAddresses = viewState.recommendedAddresses,
                        onAddressClick = { addressName ->
                            viewModel.searchAddress(addressName)
                        }
                    )
                }
            }
        }

    }
}


