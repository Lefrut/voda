package com.vodovoz.app.feature.addresses.add.composables

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.vodovoz.app.R
import com.vodovoz.app.design_system.composables.dialogs.VodovozDialog
import com.vodovoz.app.design_system.composables.placeholders.LoadingPlaceholder
import com.vodovoz.app.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.vodovoz.app.design_system.composables.snackbar.VodovozSnackbarHost
import com.vodovoz.app.design_system.composables.top_bar.VodovozTopBar
import com.vodovoz.app.feature.addresses.add.AddAddressViewModel
import com.vodovoz.app.feature.addresses.add.model.AddAddressState
import com.vodovoz.app.feature.addresses.add.model.AddAddressUiState

@Composable
fun AddAddressScreen(
    viewModel: AddAddressViewModel,
    viewState: AddAddressState,
    snackbarHostState: SnackbarHostState,
) {
    val editMode = viewState.addressId != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        VodovozTopBar(
            title = if (!editMode) stringResource(R.string.new_address) else stringResource(R.string.redact_address),
            actionIconId = if (editMode) R.drawable.ic_trash else null,
            actionIconTint = MaterialTheme.colorScheme.surfaceTint,
            onActionClick = {
                viewModel.showRemoveAddressDialog()
            },
            onBack = {
                viewModel.navigateBack()
            }
        )

        Box(
            modifier = Modifier
                .animateContentSize()
                .weight(1f)
        ) {
            when (viewState.uiState) {
                AddAddressUiState.Error -> {
                    NetworkErrorPlaceholder {
                        viewModel.fetchAddressDetails()
                    }
                }

                AddAddressUiState.Form -> {
                    AddAddressBody(
                        addressWidget = viewState.addressField,
                        gridWidgets = viewState.gridFields,
                        linearWidgets = viewState.linearFields,
                        linearSwitches = viewState.linearSwitches,
                        button = viewState.button,
                        onWidgetChange = { widget, updatedWidget ->
                            viewModel.changeWidget(widget, updatedWidget)
                        },
                        onWidgetClick = { widget ->
                            viewModel.checkWidgetOnAddress(widget)
                        },
                        onSaveClick = {
                            if (editMode) {
                                viewModel.updateAddress()
                            } else {
                                viewModel.addAddress()
                            }
                        }
                    )
                }

                AddAddressUiState.Loading -> {
                    LoadingPlaceholder()
                }
            }

            VodovozSnackbarHost(hostState = snackbarHostState)
        }

    }

    if (viewState.showRemoveAddressDialog) {
        VodovozDialog(
            title = stringResource(id = R.string.remove_address_title),
            description = stringResource(id = R.string.remove_address_description),
            acceptButtonText = stringResource(id = R.string.delete),
            cancelButtonText = stringResource(id = R.string.cancel),
            onDismiss = {
                viewModel.hideRemoveAddressDialog()
            },
            onAccept = {
                viewModel.removeAddress()
            }
        )
    }
}