package com.vodovoz.app.feature.order_recipient

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vodovoz.app.design_system.composables.placeholders.LoadingPlaceholder
import com.vodovoz.app.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.vodovoz.app.design_system.composables.top_bar.VodovozTopBar
import com.vodovoz.app.feature.order_recipient.composables.OrderRecipientBody
import com.vodovoz.app.feature.order_recipient.model.OrderRecipientState
import com.vodovoz.app.feature.order_recipient.model.OrderRecipientUiState

@Composable
fun OrderRecipientScreen(
    viewModel: OrderRecipientViewModel,
    viewState: OrderRecipientState,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        VodovozTopBar(
            onBack = { viewModel.navigateBack() },
            title = viewState.title
        )
        when (viewState.uiState) {
            OrderRecipientUiState.Error -> {
                NetworkErrorPlaceholder {
                    viewModel.fetchOrderRecipientDetails()
                }
            }

            OrderRecipientUiState.Loading -> {
                LoadingPlaceholder()
            }

            OrderRecipientUiState.Recipient -> {
                OrderRecipientBody(
                    fields = viewState.fields,
                    button = viewState.button,
                    onFieldChange = { field, updatedField ->
                        viewModel.changeField(field, updatedField)
                    },
                    onButtonClick = { button ->
                        viewModel.activateButton(button)
                    }
                )

            }
        }
    }
}