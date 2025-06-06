package com.vodovoz.app.feature.delivery_date

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.vodovoz.app.R
import com.vodovoz.app.design_system.composables.placeholders.LoadingPlaceholder
import com.vodovoz.app.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.vodovoz.app.design_system.composables.top_bar.VodovozTopBar
import com.vodovoz.app.feature.delivery_date.composables.DeliveryDateBody
import com.vodovoz.app.feature.delivery_date.model.DeliveryDateState
import com.vodovoz.app.feature.delivery_date.model.DeliveryDateUiState

@Composable
fun DeliveryDateScreen(
    viewState: DeliveryDateState,
    viewModel: DeliveryDateViewModel,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(MaterialTheme.colorScheme.background)
    ) {
        VodovozTopBar(
            title = viewState.title.ifEmpty { stringResource(R.string.delivery_time_and_date) },
            onBack = {
                viewModel.navigateBack()
            }
        )
        when (viewState.uiState) {
            DeliveryDateUiState.Error -> {
                NetworkErrorPlaceholder {
                    viewModel.fetchDeliveryDateDetails()
                }
            }

            DeliveryDateUiState.Loading -> {
                LoadingPlaceholder()
            }

            DeliveryDateUiState.Success -> {
                DeliveryDateBody(
                    modifier = Modifier.fillMaxSize(),
                    options = viewState.options,
                    selectedOption = viewState.selectedOption,
                    timeSections = viewState.timeSections,
                    selectedTimeSection = viewState.selectedTimeSection,
                    onTimeSectionSelect = { timeSection ->
                        viewModel.selectTimeSection(timeSection)
                    },
                    onOptionSelect = { dateOption ->
                        viewModel.selectDateOption(dateOption)
                    }
                )
            }
        }
    }
}