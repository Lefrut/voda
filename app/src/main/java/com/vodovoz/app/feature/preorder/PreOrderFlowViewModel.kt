package com.vodovoz.app.feature.preorder

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.R
import com.vodovoz.app.common.resources.ResourcesProvider
import com.vodovoz.app.design_system.model.widgets.WidgetUi
import com.vodovoz.app.domain.general.model.exceptions.ValidationException
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.preorder.model.FormUi
import com.vodovoz.app.feature.preorder.model.toUi
import com.vodovoz.app.ui.mvi.Event
import com.vodovoz.app.ui.mvi.FormMviViewModel
import com.vodovoz.app.ui.mvi.FormState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PreOrderFlowViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val resourcesProvider: ResourcesProvider,
) : FormMviViewModel<PreOrderFlowViewModel.PreOrderState, PreOrderFlowViewModel.PreOrderEvent>(
    PreOrderState()
) {

    private val productId = savedState.get<Long>("productId") ?: -1L

    fun fetchPreOrderData() = viewModelScope.launch {
        vodovozServiceRepository.getPreorderDetails(productId)
            .onStart { updateState { s -> s.copy(uiState = UiState.Loading) } }
            .onEach { preOrderSectionResult ->
                preOrderSectionResult.onSuccess { preOrderForm ->
                    updateState { s ->
                        s.copy(
                            uiState = UiState.Success,
                            form = preOrderForm.toUi(),
                        )
                    }
                }.onFailure {
                    updateState { s ->
                        s.copy(uiState = UiState.Error)
                    }
                }
            }.collect()
    }

    fun sendPreOrder() = viewModelScope.launch {
        if (!validateWidgets(resourcesProvider::getString)) return@launch

        val form = stateSnapshot.form

        val queries = (form.fields + form.checkbox)
            .filterIsInstance<WidgetUi>()
            .associate { widget -> widget.id to widget.value() }

        vodovozServiceRepository.sendPreorder(productId, queries).take(1).collect { result ->
            result.onSuccess { message ->
                sendEvent(PreOrderEvent.ShowSnackbar(message, true))
                sendEvent(PreOrderEvent.GoBack)
            }.onFailure { t ->
                val errorMessage = when (t) {
                    is ValidationException -> {
                        t.message ?: resourcesProvider.getString(R.string.error_message_send_failed)
                    }

                    else -> {
                        resourcesProvider.getString(R.string.error_message_send_failed)
                    }
                }
                sendEvent(PreOrderEvent.ShowSnackbar(errorMessage))
            }
        }

        sendEvent(PreOrderEvent.HideKeyboard)
    }


    fun navigateBack() = viewModelScope.launch {
        sendEvent(PreOrderEvent.GoBack)
    }

    fun navigateToWebView(url: String, title: String) = viewModelScope.launch {
        sendEvent(PreOrderEvent.GoToWebView(url, title))
    }


    @Immutable
    data class PreOrderState(
        override val form: FormUi = FormUi.Empty,
        val uiState: UiState = UiState.Loading,
    ) : FormState()

    sealed class PreOrderEvent : Event {
        data class ShowSnackbar(val message: String, val isVeryShort: Boolean = false) :
            PreOrderEvent()

        data class GoToWebView(val url: String, val title: String) : PreOrderEvent()

        data object GoBack : PreOrderEvent()
        data object HideKeyboard : PreOrderEvent()
    }

    override fun PreOrderState.withForm(form: FormUi): PreOrderState {
        return copy(form = form)
    }

    @Stable
    sealed interface UiState {
        data object Error : UiState
        data object Success : UiState
        data object Loading : UiState
    }


}