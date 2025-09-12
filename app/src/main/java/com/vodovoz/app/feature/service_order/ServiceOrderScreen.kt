package com.vodovoz.app.feature.service_order

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vodovoz.app.design_system.composables.placeholders.LoadingPlaceholder
import com.vodovoz.app.design_system.composables.placeholders.VodovozLongPlaceholder
import com.vodovoz.app.ui.compose.VodovozForm

@Composable
fun ServiceOrderScreen(
    viewModel: ServiceOrderViewModel,
    viewState: ServiceOrderViewModel.ServiceOrderState,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        when (val uiState = viewState.uiState) {
            ServiceOrderViewModel.ServiceOrderUiState.Form -> {
                VodovozForm(
                    form = viewState.form,
                    enableSpace = true,
                    onCloseClick = viewModel::navigateBack,
                    onFieldValueChange = viewModel::changeFieldValue,
                    onCheckboxClick = viewModel::changeCheckbox,
                    onUrlClick = viewModel::navigateToWebView,
                    onButtonClick = viewModel::doOrderService
                )
            }

            ServiceOrderViewModel.ServiceOrderUiState.Loading -> {
                LoadingPlaceholder()
            }

            is ServiceOrderViewModel.ServiceOrderUiState.Success -> {
                VodovozLongPlaceholder(
                    data = uiState.placeholder,
                    onButtonClick = viewModel::navigateBack,
                    onCloseClick = viewModel::navigateBack
                )
            }
        }
    }
}