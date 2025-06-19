package com.vodovoz.app.feature.delivery_date

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.vodovoz.app.R
import com.vodovoz.app.design_system.composables.button.VodovozButtonsColumn
import com.vodovoz.app.design_system.composables.date_picker.VodovozCalendarDialog
import com.vodovoz.app.design_system.composables.placeholders.LoadingPlaceholder
import com.vodovoz.app.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.vodovoz.app.design_system.composables.top_bar.VodovozTopBar
import com.vodovoz.app.feature.delivery_date.composables.DeliveryDateBody
import com.vodovoz.app.feature.delivery_date.model.DeliveryDateState
import com.vodovoz.app.feature.delivery_date.model.DeliveryDateUiState
import com.vodovoz.app.util.formatters.VodovozDateFormatters
import java.time.LocalDate

@Composable
fun DeliveryDateScreen(
    viewState: DeliveryDateState,
    viewModel: DeliveryDateViewModel,
) {

    val uiState = viewState.uiState

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            VodovozTopBar(
                title = viewState.title.ifEmpty { stringResource(R.string.delivery_time_and_date) },
                onBack = { viewModel.navigateBack() }
            )
        },
        bottomBar = {
            if (uiState is DeliveryDateUiState.Success) {
                val button = viewState.button

                VodovozButtonsColumn(
                    buttons = listOf(button),
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 24.dp
                    )
                ) {
                    viewModel.chooseDeliveryDate()
                }
            }
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        Box(modifier = Modifier.padding(top = paddingValues.calculateTopPadding())) {
            when (uiState) {
                DeliveryDateUiState.Error -> {
                    NetworkErrorPlaceholder {
                        viewModel.fetchDeliveryDateDetails()
                    }
                }

                DeliveryDateUiState.Loading -> {
                    LoadingPlaceholder()
                }

                DeliveryDateUiState.Success, DeliveryDateUiState.BodyLoading -> {
                    DeliveryDateBody(
                        modifier = Modifier.fillMaxSize(),
                        listIsLoading = uiState is DeliveryDateUiState.BodyLoading,
                        listPaddingValues = PaddingValues(bottom = paddingValues.calculateBottomPadding()),
                        options = viewState.options,
                        selectedOption = viewState.selectedOption,
                        timeSections = viewState.timeSections,
                        selectedTimeSection = viewState.selectedTimeSection,
                        selectedTimeInterval = viewState.selectedTimeInterval,
                        onTimeSectionSelect = { timeSection ->
                            viewModel.selectTimeSection(timeSection)
                        },
                        onOptionSelect = { dateOption ->
                            viewModel.selectDateOption(dateOption)
                        },
                        onTimeIntervalSelect = { deliveryTimeInterval ->
                            viewModel.selectDeliveryTimeInterval(deliveryTimeInterval)
                        },
                        onCalendarShow = {
                            viewModel.showCalendarDialog()
                        }
                    )
                }

            }

        }

    }

    if (viewState.showCalendarDialog) {

        val initialCalendarDay = remember {

            val initialDate = try {
                LocalDate.parse(viewState.selectedOption.value, VodovozDateFormatters.DMY)
            } catch (_: Throwable){
                LocalDate.now()
            }

            CalendarDay(initialDate, DayPosition.InDate)
        }

        VodovozCalendarDialog(
            initialDate = initialCalendarDay,
            onDateSelect = { date ->
                viewModel.selectCalendarDate(date)
            },
            onDismiss = {
                viewModel.hideCalendarDialog()
            },
            isSelectableDate = { date ->
                date >= LocalDate.now()
            }
        )
    }
}