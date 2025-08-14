package com.vodovoz.app.feature.questionnaires

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.R
import com.vodovoz.app.ui.mvi.Event
import com.vodovoz.app.ui.mvi.MviViewModel
import com.vodovoz.app.ui.mvi.State
import kotlinx.coroutines.flow.update
import com.vodovoz.app.common.resources.ResourcesProvider
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.VodovozPlaceholderUi
import com.vodovoz.app.design_system.model.mapToUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.domain.general.model.RequestException
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.questionnaires.model.CheckOption
import com.vodovoz.app.feature.questionnaires.model.CheckboxListUi
import com.vodovoz.app.feature.questionnaires.model.ConditionUi
import com.vodovoz.app.feature.questionnaires.model.ConditionsCheckboxListUi
import com.vodovoz.app.feature.questionnaires.model.FieldComponentUi
import com.vodovoz.app.feature.questionnaires.model.QuestionnaireComponentUi
import com.vodovoz.app.feature.questionnaires.model.SwitchUi
import com.vodovoz.app.feature.questionnaires.model.ToggleListUi
import com.vodovoz.app.feature.questionnaires.model.ToggleOption
import com.vodovoz.app.feature.questionnaires.model.errorIfInvalid
import com.vodovoz.app.feature.questionnaires.model.ifSame
import com.vodovoz.app.feature.questionnaires.model.toUi
import com.vodovoz.app.util.extensions.singleResult
import com.vodovoz.app.util.formatters.VodovozDateFormatters
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
        _state.update { s ->
            s.copy(
                uiState = QuestionnairesUiState.Loading,
                currentWho = null,
                showCancelDialog = false
            )
        }

        val welcomeDetailsResult =
            vodovozServiceRepository.getQuestionnairesWelcomeDetails().singleResult()

        welcomeDetailsResult.onSuccess { welcomeDetails ->
            _state.update { s ->
                s.copy(
                    uiState = QuestionnairesUiState.Welcome(
                        title = welcomeDetails.title,
                        image = welcomeDetails.image,
                        header = welcomeDetails.header,
                        description = welcomeDetails.description,
                        buttons = welcomeDetails.buttons.mapToUi()
                    )
                )
            }
        }.onFailure {
            _state.update { s ->
                s.copy(uiState = QuestionnairesUiState.Error)
            }
        }
    }

    private fun fetchQuestionnairesDetails() = viewModelScope.launch {
        val currentWho = stateSnapshot.currentWho ?: return@launch

        _state.update { s ->
            s.copy(uiState = QuestionnairesUiState.Loading)
        }

        val questionnairesDetailsResult =
            vodovozServiceRepository.getQuestionnairesDetails(currentWho).singleResult()

        questionnairesDetailsResult.onSuccess { questionnairesDetails ->

            _state.update { s ->
                s.copy(
                    button = questionnairesDetails.button.toUi(),
                    components = questionnairesDetails.items.mapNotNull { it.toUi() },
                    title = questionnairesDetails.title,
                    uiState = QuestionnairesUiState.Body
                )
            }

        }.onFailure {
            delay(250)
            fetchWelcomeDetails()
        }
    }

    fun updateText(id: String, newValue: String) = viewModelScope.launch {
        mapThenUpdateComponents {
            ifSame<FieldComponentUi>(id) {
                copy(ui = ui.copy(value = newValue, isError = false))
            }
        }
    }


    fun updateSwitch(id: String, option: String) = viewModelScope.launch {
        mapThenUpdateComponents {
            ifSame<SwitchUi>(id) {
                copy(
                    selectedOption = option,
                    error = false
                )
            }
        }
    }

    fun updateCheckbox(id: String, option: CheckOption) = viewModelScope.launch {
        mapThenUpdateComponents {
            ifSame<CheckboxListUi>(id) {
                val updatedOptions = options.map { opt ->
                    if (opt.label == option.label) opt.copy(isChecked = !opt.isChecked)
                    else opt
                }
                copy(options = updatedOptions, error = false)
            }
        }
    }

    fun updateToggle(id: String, option: ToggleOption) = viewModelScope.launch {
        mapThenUpdateComponents {
            ifSame<ToggleListUi>(id) {
                val updatedOptions = options.map { opt ->
                    opt.copy(isSelected = (opt.label == option.label))
                }
                copy(options = updatedOptions, error = false)
            }
        }
    }

    fun updateConditionCheckbox(id: String, option: CheckOption) = viewModelScope.launch {
        mapThenUpdateComponents {
            ifSame<ConditionsCheckboxListUi>(id) {
                val updatedOptions = options.map { opt ->
                    if (opt.label == option.label) opt.copy(isChecked = !opt.isChecked)
                    else opt
                }
                copy(options = updatedOptions, error = false)
            }
        }
    }

    fun checkBirthdayField(component: FieldComponentUi) = viewModelScope.launch {
        if (component.ui.id != "DR") return@launch

        _state.update { s ->
            s.copy(
                showDatePicker = true,
                currentDateField = component
            )
        }

    }

    fun closeDatePicker() = viewModelScope.launch {
        _state.update { s ->
            s.copy(showDatePicker = false)
        }
    }

    private fun mapThenUpdateComponents(
        onComponentChange: QuestionnaireComponentUi.() -> QuestionnaireComponentUi,
    ) {
        _state.update { s ->
            s.copy(
                components = stateSnapshot.components.map { component ->
                    onComponentChange(component)
                }
            )
        }
    }


    fun navigateBack() = viewModelScope.launch {
        val uiState = stateSnapshot.uiState
        if (uiState is QuestionnairesUiState.Body
            || stateSnapshot.currentWho != null
            && uiState !is QuestionnairesUiState.Success
        ) {
            showCancelDialog()
        } else {
            sendEvent(QuestionnaireEvents.GoBack)
        }
    }

    private fun showCancelDialog() = viewModelScope.launch {
        _state.update { s ->
            s.copy(showCancelDialog = true)
        }
    }

    fun closeCancelDialog() = viewModelScope.launch {
        _state.update { s ->
            s.copy(showCancelDialog = false)
        }
    }


    fun activateWelcomeButton(btn: ColorfulButtonUi) = viewModelScope.launch {
        _state.update { s ->
            s.copy(currentWho = btn.id)
        }
        fetchQuestionnairesDetails()
    }

    fun sendAnswers() = viewModelScope.launch {

        mapThenUpdateComponents {
            errorIfInvalid { resourcesProvider.getString(it) }
        }

        val currentWho = stateSnapshot.currentWho
        val components = stateSnapshot.components
        if (components.any { component -> component.error } || currentWho == null) {
            sendEvent(QuestionnaireEvents.ScrollToTop)
            return@launch
        }

        val answers = components.toAnswerString()

        _state.update { s ->
            s.copy(button = s.button.copy(loading = true))
        }

        val sendQuestionnairesResult =
            vodovozServiceRepository.sendQuestionnairesAnswers(
                currentWho, answers
            ).singleResult()

        sendQuestionnairesResult.onSuccess {
            val placeholder = it.toUi()
            _state.update { s ->
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

            _state.update { s ->
                s.copy(button = s.button.copy(loading = false))
            }

        }
    }

    private fun List<QuestionnaireComponentUi>.toAnswerString(): String {
        return this
            .mapNotNull { comp ->
                val raw = when (comp) {
                    is FieldComponentUi -> comp.ui.value
                    is SwitchUi -> comp.selectedOption
                    is CheckboxListUi -> comp.options
                        .filter { it.isChecked }
                        .joinToString(",") { it.label }

                    is ToggleListUi -> comp.options
                        .firstOrNull { it.isSelected }
                        ?.label
                        .orEmpty()

                    is ConditionsCheckboxListUi -> comp.options
                        .filter { it.isChecked }
                        .joinToString(",") { it.label }
                }
                if (raw.isBlank()) return@mapNotNull null

                val cleaned = raw
                    .removePrefix("[")
                    .removeSuffix("]")
                    .replace(", ", ",")
                    .trim()

                "${comp.id}\$$cleaned"
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

        _state.update { s ->
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
        val components: List<QuestionnaireComponentUi> = emptyList(),
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