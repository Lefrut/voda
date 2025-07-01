package com.vodovoz.app.feature.addresses

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vodovoz.app.R
import com.vodovoz.app.design_system.composables.button.VodovozButton
import com.vodovoz.app.design_system.composables.dialogs.VodovozDialog
import com.vodovoz.app.design_system.composables.placeholders.LoadingPlaceholder
import com.vodovoz.app.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.vodovoz.app.design_system.composables.placeholders.VodovozPlaceholder
import com.vodovoz.app.design_system.composables.top_bar.VodovozTopBar
import com.vodovoz.app.feature.addresses.composables.AddressesBody
import com.vodovoz.app.feature.addresses.model.AddressScreenTypeUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressesScreen(
    viewModel: AddressesFlowViewModel,
    viewState: AddressesFlowViewModel.AddressesState,
) {
    val uiState = viewState.uiState
    val pullRefreshState = rememberPullToRefreshState()

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
                VodovozButton(
                    modifier = Modifier.padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
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
                                viewModel.navigateToOrdering()
                            }
                        }
                    }
                )
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
                PullToRefreshBox(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    state = pullRefreshState,
                    isRefreshing = viewState.showRefreshIndicator,
                    indicator = {
                        Indicator(
                            modifier = Modifier.align(Alignment.TopCenter),
                            isRefreshing = viewState.showRefreshIndicator,
                            state = pullRefreshState,
                            containerColor = MaterialTheme.colorScheme.background,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    onRefresh = {
                        viewModel.refresh()
                    }
                ) {
                    AddressesBody(
                        contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 24.dp),
                        screenTypeUi = viewState.screenType,
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