package com.vodovoz.app.feature.service_order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vodovoz.app.design_system.composables.placeholders.LoadingPlaceholder
import com.vodovoz.app.design_system.composables.top_bar.VodovozTopBar
import com.vodovoz.app.feature.service_order.composables.ServiceOrderBody

@Composable
fun ServiceOrderScreen(
    viewModel: ServiceOrderViewModel,
    viewState: ServiceOrderViewModel.ServiceOrderState,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        VodovozTopBar(
            onBack = { viewModel.navigateBack() },
            title = viewState.title
        )

        when (viewState.uiState) {
            ServiceOrderViewModel.ServiceOrderUiState.Loading -> {
                LoadingPlaceholder()
            }

            ServiceOrderViewModel.ServiceOrderUiState.Form -> {
                ServiceOrderBody(
                    modifier = Modifier.weight(1f),
                    subtitle = viewState.subtitle,
                    fields = viewState.fields,
                    button = viewState.button,
                    onButtonClick = {
                        viewModel.doOrderService()
                    },
                    onFieldChange = { field, updatedField ->
                        viewModel.changeField(field, updatedField)
                    }
                )

            }

            is ServiceOrderViewModel.ServiceOrderUiState.Success -> {

            }
        }

    }
}