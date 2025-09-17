package com.m.vodovoz.feature.delivery_date

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.m.vodovoz.design_system.composables.button.VodovozButtonsColumn
import com.m.vodovoz.design_system.composables.date_picker.VodovozCalendarDialog
import com.m.vodovoz.design_system.composables.floating.BottomFloatingContainer
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.composables.top_bar.VodovozTopBar
import com.m.vodovoz.design_system.model.widgets.CheckboxUi
import com.m.vodovoz.feature.delivery_date.composables.DeliveryDateBody
import com.m.vodovoz.feature.delivery_date.model.DeliveryDateState
import com.m.vodovoz.feature.delivery_date.model.DeliveryDateUiState
import com.m.vodovoz.util.formatters.VodovozDateFormatters
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
                title = viewState.title,
                onBack = { viewModel.navigateBack() }
            )
        },
        bottomBar = {
            if (uiState is DeliveryDateUiState.Success) {
                val button = viewState.button
                val earlierCheckbox = viewState.earlierCheckbox

                BottomFloatingContainer {
                    if (earlierCheckbox != null) {
                        EarlierCheckbox(
                            modifier = Modifier.padding(start = 16.dp, bottom = 12.dp, end = 16.dp),
                            checkbox = earlierCheckbox,
                            onCheckedChange = { value ->
                                viewModel.changeCheckbox(
                                    earlierCheckbox.copy(checked = value)
                                )
                            }
                        )
                    }

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
                        listPaddingValues = PaddingValues(
                            bottom = paddingValues.calculateBottomPadding()
                        ),
                        options = viewState.options,
                        selectedOption = viewState.selectedDateOption,
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
                LocalDate.parse(viewState.selectedDateOption.value, VodovozDateFormatters.DMY)
            } catch (_: Throwable) {
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

@Composable
private fun EarlierCheckbox(
    modifier: Modifier = Modifier,
    checkbox: CheckboxUi,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(modifier = modifier.clickable(interactionSource = null, indication = null) {
        onCheckedChange(!checkbox.checked)
    }, verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            modifier = Modifier
                .padding(end = 16.dp)
                .size(24.dp),
            checked = checkbox.checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkmarkColor = MaterialTheme.colorScheme.background,
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.outline
            )
        )

        Text(
            text = checkbox.name,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}