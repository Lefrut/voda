package com.vodovoz.app.feature.questionnaires

import com.vodovoz.app.design_system.composables.date_picker.VodovozCalendarDialog
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.vodovoz.app.util.formatters.VodovozDateFormatters
import com.vodovoz.app.design_system.composables.snackbar.VodovozSnackbarHost
import com.vodovoz.app.design_system.composables.top_bar.VodovozTopBar
import com.vodovoz.app.feature.questionnaires.components.QuestionnairesBody
import java.time.LocalDate

@Composable
fun QuestionnairesScreen(
    viewModel: QuestionnairesFlowViewModel,
    viewState: QuestionnairesFlowViewModel.QuestionnaireState,
    scrollState: ScrollState,
    snackbarHostState: SnackbarHostState,
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .fillMaxSize()
    ) {
        VodovozTopBar(
            onBack = { viewModel.navigateBack() },
            title = viewState.title
        )
        QuestionnairesBody(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState),
            components = viewState.components,
            button = viewState.button,
            onButtonClick = {
                viewModel.sendAnswers()
            },
            onFieldChange = { field, newValue ->
                viewModel.updateText(field.id, newValue.value)
            },
            onSwitchChange = { switch, option ->
                viewModel.updateSwitch(switch.id, option)
            },
            onToggleChange = { toggle, option ->
                viewModel.updateToggle(toggle.id, option)
            },
            onCheckboxChange = { list, option ->
                viewModel.updateCheckbox(list.id, option)
            },
            onConditionCheckboxChange = { conditionList, option ->
                viewModel.updateConditionCheckbox(conditionList.id, option)
            },
            onConditionClick = { condition ->
                viewModel.navigateToWebView(condition)
            },
            onFieldClick = { fieldComponent ->
                viewModel.checkBirthdayField(fieldComponent)
            }
        )

        VodovozSnackbarHost(snackbarHostState)
    }


    val currentDateField = viewState.currentDateField

    if (viewState.showDatePicker && currentDateField != null ) {

        val currentDay = rememberSaveable(currentDateField.ui.value) {
            val localDate = runCatching {
                LocalDate.parse(currentDateField.ui.value, VodovozDateFormatters.DMY)
            }.getOrNull() ?: LocalDate.now().minusYears(36)

            return@rememberSaveable CalendarDay(localDate, DayPosition.MonthDate)
        }


        val today = remember { LocalDate.now() }

        VodovozCalendarDialog(
            initialDate = currentDay,
            isSelectableDate = { date ->
                date < today
            },
            onDateSelect = { selectedDate ->
                viewModel.changeDate(selectedDate)
            },
            onDismiss = {
                viewModel.closeDatePicker()
            }
        )
    }



}