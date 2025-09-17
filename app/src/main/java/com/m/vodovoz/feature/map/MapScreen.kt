package com.m.vodovoz.feature.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.m.vodovoz.R
import com.m.vodovoz.design_system.composables.dialogs.VodovozDialog
import com.m.vodovoz.feature.map.composables.MapBody
import com.m.vodovoz.feature.map.composables.MapDeliveryBottomSheet
import com.m.vodovoz.feature.map.composables.MapSearchList
import com.m.vodovoz.feature.map.composables.MapTopBar
import com.m.vodovoz.ui.yandex_map.YandexMapUi

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

    if (viewState.showDeliveryBS && viewState.deliveryPopupWindow != null) {
        MapDeliveryBottomSheet(
            data = viewState.deliveryPopupWindow,
            onDismissRequest = {
                viewModel.closeDeliveryBottomSheet()
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
                addressName = viewState.currentMapAddress?.name ?: "",
                addressIsLoading = viewState.addressIsLoading,
                addressIsError = viewState.addressIsError,
                screenType = viewState.screenType,
                deliveryButton = viewState.deliveryButton,
                yandexMap = yandexMap,
                areas = viewState.areas,
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
                },
                onDeliveryButtonClick = {
                    viewModel.showDeliveryBottomSheet()
                }
            )

            Column(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(
                    modifier = Modifier.fillMaxSize(),
                    visible = viewState.mode is MapFlowViewModel.MapUiMode.Search && viewState.query.isNotBlank() && viewState.recommendedAddresses.isNotEmpty(),
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


