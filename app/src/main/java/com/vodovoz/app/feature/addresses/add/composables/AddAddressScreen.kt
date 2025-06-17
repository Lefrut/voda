package com.vodovoz.app.feature.addresses.add.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.vodovoz.app.R
import com.vodovoz.app.design_system.composables.dialogs.VodovozDialog
import com.vodovoz.app.design_system.composables.top_bar.VodovozTopBar
import com.vodovoz.app.feature.addresses.add.AddAddressViewModel
import com.vodovoz.app.feature.addresses.add.model.AddAddressState

@Composable
fun AddAddressScreen(viewModel: AddAddressViewModel, viewState: AddAddressState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        VodovozTopBar(
            title = "",
            actionIconId = R.drawable.icon_trash,
            actionIconTint = MaterialTheme.colorScheme.surfaceTint,
            onActionClick = {
                viewModel.showRemoveAddressDialog()
            },
            onBack = {
                viewModel.navigateBack()
            }
        )

        AddAddressBody(
            fields = viewState.fields,
            widgets = viewState.widgets,
            onWidgetChange = { widget, updatedWidget ->
                viewModel.changeWidget(widget, updatedWidget)
            },
            onSaveClick = {
                viewModel.updateAddress()
            }
        )
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