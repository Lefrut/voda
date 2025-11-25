package com.m.vodovoz.feature.questionnaires

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.R
import com.m.vodovoz.common.resources.ResourcesProvider
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.model.VodovozPlaceholderUi
import com.m.vodovoz.design_system.model.mapToUi
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.domain.general.model.exceptions.RequestException
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.questionnaires.model.CheckboxListUi
import com.m.vodovoz.feature.questionnaires.model.ComponentOptionUi
import com.m.vodovoz.feature.questionnaires.model.ConditionUi
import com.m.vodovoz.feature.questionnaires.model.ConditionsCheckboxListUi
import com.m.vodovoz.feature.questionnaires.model.FieldComponentUi
import com.m.vodovoz.feature.questionnaires.model.QuizComponentUi
import com.m.vodovoz.feature.questionnaires.model.QuizWarningsVisitor
import com.m.vodovoz.feature.questionnaires.model.SwitchUi
import com.m.vodovoz.feature.questionnaires.model.ToggleListUi
import com.m.vodovoz.feature.questionnaires.model.applyOtherVisibilityRules
import com.m.vodovoz.feature.questionnaires.model.toUi
import com.m.vodovoz.feature.questionnaires.model.update
import com.m.vodovoz.feature.questionnaires.model.updateIfSame
import com.m.vodovoz.ui.mvi.Event
import com.m.vodovoz.ui.mvi.MviViewModel
import com.m.vodovoz.ui.mvi.State
import com.m.vodovoz.util.extensions.handleResultFlow
import com.m.vodovoz.util.extensions.singleResult
import com.m.vodovoz.util.formatters.VodovozDateFormatters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class QuestionnairesFlowViewModel @Inject constructor(
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val resourcesProvider: ResourcesProvider,
) : MviViewModel<QuestionnairesFlowViewModel.QuestionnaireState, QuestionnairesFlowViewModel.QuestionnaireEvents>(
    QuestionnaireState()
) {

    init {
        viewModelScope.launch { delay(200) }.invokeOnCompletion {
            fetchWelcomeDetails()
        }
    }


    fun fetchWelcomeDetails() = viewModelScope.launch {
        handleResultFlow(
            request = {
                vodovozServiceRepository.getQuestionnairesWelcomeDetails()
            },
            transform = {
                QuestionnairesUiState.Welcome(
                    title = title,
                    image = image,
                    header = header,
                    description = description,
                    buttons = buttons.mapToUi()
                )
            },
            success = { uiState ->
                updateState { s ->
                    s.copy(uiState = uiState)
                }
            },
            failure = {
                updateState { s ->
                    s.copy(uiState = QuestionnairesUiState.Error)
                }
            }
        )
    }

    private fun fetchQuestionnairesDetails() = viewModelScope.launch {
        handleResultFlow(
            request = {
                val currentWho = stateSnapshot.currentWho ?: return@launch

                updateState { s ->
                    s.copy(uiState = QuestionnairesUiState.Loading)
                }

                vodovozServiceRepository.getQuestionnairesDetails(currentWho)
            },
            transform = { toUi() },
            success = { questionnaireDetails ->
                updateState {
                    stateSnapshot.copy(
                        button = questionnaireDetails.button,
                        components = questionnaireDetails.items.applyOtherVisibilityRules(),
                        title = questionnaireDetails.title,
                        uiState = QuestionnairesUiState.Body
                    )
                }
            },
            failure = {
                delay(250)
                fetchWelcomeDetails().join()
            }
        )
    }

    fun updateText(id: String, newValue: String) = viewModelScope.launch {
        mapThenUpdateComponents {
            updateIfSame<FieldComponentUi>(id) {
                copy(ui = ui.copy(value = newValue, isError = false))
            }
        }
    }


    fun updateSwitch(id: String, option: String) = viewModelScope.launch {
        mapThenUpdateComponents {
            updateIfSame<SwitchUi>(id) {
                copy(
                    selectedOption = option,
                    error = false
                )
            }
        }
    }

    fun <T : QuizComponentUi> updateOptions(
        component: T,
        option: ComponentOptionUi,
    ) = viewModelScope.launch {
        val updatedComponents = stateSnapshot.components.map {
            it.updateIfSame<CheckboxListUi, ToggleListUi, ConditionsCheckboxListUi>(
                id = component.id,
                onSame1 = { update(option) { options -> copy(options = options, error = false) } },
                onSame2 = { update(option) { options -> copy(options = options, error = false) } },
                onSame3 = { update(option) { options -> copy(options = options, error = false) } }
            )
        }

        val updatedComponentsByRules = updatedComponents.applyOtherVisibilityRules()
        updateState { state ->
            state.copy(components = updatedComponentsByRules)
        }
    }


    fun checkBirthdayField(component: FieldComponentUi) = viewModelScope.launch {
        val field = component.ui

        if (field.id == "DR") {
            updateState { s ->
                s.copy(
                    showDatePicker = true,
                    currentDateField = component
                )
            }
        }
    }

    fun closeDatePicker() = viewModelScope.launch {
        updateState { s ->
            s.copy(showDatePicker = false)
        }
    }

    private fun mapThenUpdateComponents(
        transform: QuizComponentUi.() -> QuizComponentUi,
    ) {
        updateState { s ->
            s.copy(
                components = stateSnapshot.components.map { component ->
                    transform(component)
                }
            )
        }
    }


    fun navigateBack() = viewModelScope.launch {
        val uiState = stateSnapshot.uiState
        if (uiState is QuestionnairesUiState.Body) {
            showCancelDialog()
        } else {
            sendEvent(QuestionnaireEvents.GoBack)
        }
    }

    private fun showCancelDialog() = viewModelScope.launch {
        updateState { s ->
            s.copy(showCancelDialog = true)
        }
    }

    fun closeCancelDialog() = viewModelScope.launch {
        updateState { s ->
            s.copy(showCancelDialog = false)
        }
    }


    fun activateWelcomeButton(btn: ColorfulButtonUi) = viewModelScope.launch {
        updateState { s ->
            s.copy(currentWho = btn.id)
        }
        fetchQuestionnairesDetails().join()
    }

    fun sendAnswers() = viewModelScope.launch {

        mapThenUpdateComponents {
            updateByValidation(QuizWarningsVisitor(resourcesProvider::getString))
        }

        val currentWho = stateSnapshot.currentWho
        val components = stateSnapshot.components
        if (components.any { component -> component.error } || currentWho == null) {
            sendEvent(QuestionnaireEvents.ScrollToTop)
            return@launch
        }

        val answers = components.toAnswerString()

        updateState { s ->
            s.copy(button = s.button.copy(loading = true))
        }

        val sendQuestionnairesResult =
            vodovozServiceRepository.sendQuestionnairesAnswers(
                currentWho, answers
            ).singleResult()

        sendQuestionnairesResult.onSuccess {
            val placeholder = it.toUi()
            updateState { s ->
                s.copy(
                    uiState = QuestionnairesUiState.Success(placeholder),
                    button = s.button.copy(loading = false)
                )
            }
        }.onFailure { t ->
            if (t is RequestException) {
                sendEvent(QuestionnaireEvents.ShowToast(t.message ?: ""))
            } else {
                sendEvent(
                    QuestionnaireEvents.ShowToast(
                        resourcesProvider.getString(R.string.questionnaire_failed)
                    )
                )
            }

            updateState { s ->
                s.copy(button = s.button.copy(loading = false))
            }

        }
    }

    private fun List<QuizComponentUi>.toAnswerString(): String {
        return this
            .mapNotNull { comp ->
                val raw = when (comp) {
                    is FieldComponentUi -> comp.ui.value
                    is SwitchUi -> comp.selectedOption
                    is CheckboxListUi -> comp.options
                        .filter { it.value }
                        .joinToString(",") { it.label }

                    is ToggleListUi -> comp.options
                        .firstOrNull { it.value }
                        ?.label
                        .orEmpty()

                    is ConditionsCheckboxListUi -> comp.options
                        .filter { it.value }
                        .joinToString(",") { it.label }
                }
                if (raw.isBlank()) return@mapNotNull null

                val cleaned = raw
                    .removePrefix("[")
                    .removeSuffix("]")
                    .replace(", ", ",")
                    .trim()

                "${comp.id}$$cleaned"
            }
            .joinToString(separator = ";", postfix = ";")
    }

    fun navigateToWebView(condition: ConditionUi) = viewModelScope.launch {
        sendEvent(QuestionnaireEvents.GoToWebView(condition.url))
    }

    fun changeDate(selectedDate: LocalDate) = viewModelScope.launch {
        val value = kotlin.runCatching { selectedDate.format(VodovozDateFormatters.DMY) }
            .getOrElse { "" }
        val currentDateField = stateSnapshot.currentDateField ?: return@launch
        val updatedCurrentDateField = currentDateField.copy(
            ui = currentDateField.ui.copy(value = value, isError = false)
        )

        updateState { s ->
            s.copy(
                showDatePicker = false,
                currentDateField = updatedCurrentDateField,
                components = s.components.map {
                    if (it.id == updatedCurrentDateField.id) updatedCurrentDateField else it
                }
            )
        }
    }


    @Immutable
    data class QuestionnaireState(
        val uiState: QuestionnairesUiState = QuestionnairesUiState.Loading,
        val currentWho: String? = null,
        val title: String = "",
        val button: ColorfulButtonUi = ColorfulButtonUi.Empty,
        val components: List<QuizComponentUi> = emptyList(),
        val showDatePicker: Boolean = false,
        val currentDateField: FieldComponentUi? = null,
        val showCancelDialog: Boolean = false,
    ) : State

    sealed class QuestionnaireEvents : Event {
        data class GoToWebView(val url: String) : QuestionnaireEvents()
        data class ShowToast(val message: String) : QuestionnaireEvents()

        data object GoBack : QuestionnaireEvents()
        data object ScrollToTop : QuestionnaireEvents()
    }

    @Stable
    sealed interface QuestionnairesUiState {

        data object Loading : QuestionnairesUiState
        data class Welcome(
            val title: String,
            val image: String,
            val header: String,
            val description: String,
            val buttons: List<ColorfulButtonUi>,
        ) : QuestionnairesUiState

        data object Body : QuestionnairesUiState
        data class Success(val placeholder: VodovozPlaceholderUi) : QuestionnairesUiState

        data object Error : QuestionnairesUiState

    }
}