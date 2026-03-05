package com.m.vodovoz.feature.questionnaires

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.R
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.common.resources.ResourcesProvider
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.model.VodovozPlaceholderUi
import com.m.vodovoz.design_system.model.mapToUi
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.domain.general.model.exceptions.RequestException
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.questionnaires.model.ConditionUi
import com.m.vodovoz.feature.questionnaires.model.FieldComponentUi
import com.m.vodovoz.feature.questionnaires.model.QuizComponentUi
import com.m.vodovoz.feature.questionnaires.model.QuizWarningsVisitor
import com.m.vodovoz.feature.questionnaires.model.applyOtherVisibilityRules
import com.m.vodovoz.feature.questionnaires.model.toUi
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
    val tabManager: TabManager,
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

    fun updateComponent(updatedComponent: QuizComponentUi) {
        mapThenUpdateComponents {
            if (id == updatedComponent.id) updatedComponent.withError(false)
            else this
        }
    }


    fun checkBirthdayField(component: FieldComponentUi) {
        if (component.ui.id != "DR") return

        updateState { s ->
            s.copy(
                showDatePicker = true,
                currentDateField = component
            )
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
                }.applyOtherVisibilityRules()
            )
        }
    }


    fun navigateBack() = viewModelScope.launch {
        when (stateSnapshot.uiState) {
            QuestionnairesUiState.Body -> {
                showCancelDialog()
            }

            else -> sendEvent(QuestionnaireEvents.GoBack)
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

        val components = stateSnapshot.components
        val currentWho = stateSnapshot.currentWho


        if (components.any { component -> component.error } || currentWho == null) {
            sendEvent(QuestionnaireEvents.ScrollToTop).also { return@launch }
        }

        updateState { s ->
            s.copy(button = s.button.copy(loading = true))
        }

        val formattedAnswer = components.formatToAnswer()
        vodovozServiceRepository.sendQuestionnairesAnswers(
            who = currentWho,
            answers = formattedAnswer
        ).singleResult().onSuccess {
            val placeholder = it.toUi()
            updateState { s ->
                s.copy(
                    uiState = QuestionnairesUiState.Success(placeholder),
                    button = s.button.copy(loading = false)
                )
            }
        }.onFailure { t ->
            val errorMessage = if (t is RequestException) {
                t.message
            } else {
                resourcesProvider.getString(R.string.questionnaire_failed)
            }

            sendEvent(QuestionnaireEvents.ShowToast(errorMessage.orEmpty()))

            updateState { s ->
                s.copy(button = s.button.copy(loading = false))
            }
        }
    }

    private fun List<QuizComponentUi>.formatToAnswer(): String {
        return mapNotNull { comp ->
            val raw = comp.value()
            if (raw.isBlank()) return@mapNotNull null

            val cleaned = raw
                .removePrefix("[")
                .removeSuffix("]")
                .replace(", ", ",")
                .trim()

            "${comp.id}$$cleaned"
        }.joinToString(separator = ";", postfix = ";")
    }

    fun navigateToWebView(condition: ConditionUi) = viewModelScope.launch {
        sendEvent(QuestionnaireEvents.GoToWebView(condition.url))
    }

    fun changeDate(selectedDate: LocalDate) = viewModelScope.launch {
        val value = kotlin.runCatching {
            selectedDate.format(VodovozDateFormatters.DMY)
        }.getOrElse { "" }
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
