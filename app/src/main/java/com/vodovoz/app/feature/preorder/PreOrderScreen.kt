package com.vodovoz.app.feature.preorder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vodovoz.app.design_system.composables.top_bar.ClosingTopBar
import com.vodovoz.app.feature.preorder.composables.PreOrderBody

@Composable
fun PreOrderScreen(
    viewModel: PreOrderFlowViewModel,
    viewState: PreOrderFlowViewModel.PreOrderState,
    snackbarHostState: SnackbarHostState,
) {

    val form = viewState.form
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ClosingTopBar(title = form.title, onCloseClick = { viewModel.navigateBack() })

        PreOrderBody(
            colorfulButton = form.colorfulButton,
            description = form.description,
            fields = form.fields,
            checkbox = form.checkbox,
            snackbarHostState = snackbarHostState,
            onOrderSend = { viewModel.sendPreOrder() },
            onFieldValueChange = { field, newValue ->
                viewModel.changeFieldValue(field, newValue)
            },
            onUrlClick = viewModel::navigateToWebView,
            onCheckboxClick = viewModel::changeCheckbox
        )

    }
}