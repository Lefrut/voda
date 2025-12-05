package com.m.vodovoz.feature.addresses

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.m.vodovoz.R
import com.m.vodovoz.design_system.composables.button.VodovozButton
import com.m.vodovoz.design_system.composables.dialogs.VodovozDialog
import com.m.vodovoz.design_system.composables.floating.BottomFloatingContainer
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.VodovozPlaceholder
import com.m.vodovoz.design_system.composables.pull_to_refresh.VodovozPullToRefreshBox
import com.m.vodovoz.design_system.composables.top_bar.VodovozTopBar
import com.m.vodovoz.feature.addresses.composables.AddressesBody
import com.m.vodovoz.feature.addresses.model.AddressScreenTypeUi
import com.m.vodovoz.feature.addresses.model.AddressUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressesScreen(
    viewModel: AddressesFlowViewModel,
    viewState: AddressesFlowViewModel.AddressesState,
) {
    val uiState = viewState.uiState

    Scaffold(
        topBar = {
            VodovozTopBar(
                onBack = {
                    viewModel.navigateBack()
                },
                title = when (viewState.screenType) {
                    AddressScreenTypeUi.Add -> stringResource(R.string.addresses)
                    AddressScreenTypeUi.Choose -> stringResource(R.string.delivery_address)
                },
                actionIconId = when (viewState.screenType) {
                    AddressScreenTypeUi.Add -> null
                    AddressScreenTypeUi.Choose -> R.drawable.ic_plus_rounded
                },
                onActionClick = {
                    viewModel.addAddress()
                }
            )
        },
        bottomBar = {
            if (uiState is AddressesFlowViewModel.AddressesUiState.Success) {
                BottomFloatingContainer {
                    VodovozButton(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = when (viewState.screenType) {
                            AddressScreenTypeUi.Add -> stringResource(R.string.add_address)
                            AddressScreenTypeUi.Choose -> stringResource(R.string.choose)
                        },
                        onClick = {
                            when (viewState.screenType) {
                                AddressScreenTypeUi.Add -> {
                                    viewModel.addAddress()
                                }

                                AddressScreenTypeUi.Choose -> {
                                    viewModel.searchThenNavigateToOrdering()
                                }
                            }
                        },
                        isLoading = viewState.buttonLoading,
                        enabled = viewState.buttonEnabled && viewState.selectedAddress != AddressUi.Empty
                    )
                }
            }

        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        when (uiState) {
            is AddressesFlowViewModel.AddressesUiState.Empty -> {
                VodovozPlaceholder(
                    modifier = Modifier.padding(paddingValues),
                    data = uiState.placeholder,
                    onButtonClick = {
                        viewModel.addAddress()
                    }
                )
            }

            AddressesFlowViewModel.AddressesUiState.Error -> {
                NetworkErrorPlaceholder(modifier = Modifier.padding(paddingValues)) { viewModel.fetchAddresses() }
            }

            AddressesFlowViewModel.AddressesUiState.Loading -> {
                LoadingPlaceholder(modifier = Modifier.padding(paddingValues))
            }

            AddressesFlowViewModel.AddressesUiState.Success -> {
                VodovozPullToRefreshBox(
                    modifier = Modifier.padding(paddingValues),
                    isRefreshing = viewState.showRefreshIndicator,
                    onRefresh = {
                        viewModel.refresh()
                    }
                ) {
                    AddressesBody(
                        contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 24.dp),
                        screenTypeUi = viewState.screenType,
                        removableAddressId = viewState.currentRemoveAddress?.id,
                        addressSections = viewState.addressSections,
                        selectedAddress = viewState.selectedAddress,
                        onEditAddressClick = { address ->
                            viewModel.editAddress(address)
                        },
                        onAddressSelect = { address ->
                            viewModel.selectAddress(address)
                        },
                        onRemoveAddressSwipe = { address ->
                            viewModel.showRemoveAddressDialog(address)
                        }
                    )
                }
            }
        }
    }

    if (viewState.showRemoveAddressDialog && viewState.currentRemoveAddress != null) {
        VodovozDialog(
            title = stringResource(id = R.string.remove_address_title),
            description = stringResource(
                id = R.string.remove_address_description,
            ),
            acceptButtonText = stringResource(id = R.string.delete),
            cancelButtonText = stringResource(id = R.string.cancel),
            onDismiss = {
                viewModel.hideRemoveAddressDialog()
            },
            onAccept = {
                viewModel.removeAddress(viewState.currentRemoveAddress)
            }
        )
    }

}