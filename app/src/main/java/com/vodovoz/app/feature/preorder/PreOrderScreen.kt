package com.vodovoz.app.feature.preorder

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import com.vodovoz.app.ui.compose.VodovozForm

@Composable
fun PreOrderScreen(
    viewModel: PreOrderFlowViewModel,
    viewState: PreOrderFlowViewModel.PreOrderState,
    snackbarHostState: SnackbarHostState,
) {
    val form = viewState.form

    VodovozForm(
        form = form,
        enableSpace = false,
        snackbarHostState = snackbarHostState,
        onCloseClick = viewModel::navigateBack,
        onFieldValueChange = viewModel::changeFieldValue,
        onCheckboxClick = viewModel::changeCheckbox,
        onUrlClick = viewModel::navigateToWebView,
        onButtonClick = viewModel::sendPreOrder
    )
}