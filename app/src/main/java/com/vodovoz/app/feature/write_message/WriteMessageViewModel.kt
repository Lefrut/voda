package com.vodovoz.app.feature.write_message

import androidx.lifecycle.viewModelScope
import com.vodovoz.app.R
import com.vodovoz.app.common.resources.ResourcesProvider
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.design_system.model.widgets.WidgetUi
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.preorder.model.FormUi
import com.vodovoz.app.feature.preorder.model.toUi
import com.vodovoz.app.feature.write_message.model.WriteMessageEvent
import com.vodovoz.app.feature.write_message.model.WriteMessageState
import com.vodovoz.app.feature.write_message.model.WriteMessageUiState
import com.vodovoz.app.ui.mvi.FormMviViewModel
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WriteMessageViewModel @Inject constructor(
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val resourcesProvider: ResourcesProvider,
) : FormMviViewModel<WriteMessageState, WriteMessageEvent>(WriteMessageState()) {

    init {
        fetchWriteMessageDetails()
    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(WriteMessageEvent.GoBack)
    }

    fun fetchWriteMessageDetails() = viewModelScope.launch {
        updateState { s ->
            s.copy(uiState = WriteMessageUiState.Loading)
        }

        vodovozServiceRepository.getWriteMessageDetails().singleResult().onSuccess { form ->
            updateState { s ->
                s.copy(
                    uiState = WriteMessageUiState.Body,
                    form = form.toUi()
                )
            }
        }.onFailure {
            updateState { s ->
                s.copy(uiState = WriteMessageUiState.Error)
            }

        }
    }

    fun sendMessage() = viewModelScope.launch {
        if (!validateWidgets(resourcesProvider::getString)) return@launch

        updateForm { copy(button = button.copy(loading = true)) }

        val queries = with(stateSnapshot.form) {
            fields + checkbox
        }.filterIsInstance<WidgetUi>().associate { it.id to it.value() }

        vodovozServiceRepository.sendMessage(
            queries
        ).singleResult().onSuccess { placeholder ->
            updateState { s ->
                val form = s.form
                s.copy(
                    uiState = WriteMessageUiState.Success(placeholder.toUi()),
                    form = form.copy(
                        button = form.button.copy(loading = false)
                    )
                )
            }
        }.onFailure {
            sendEvent(
                WriteMessageEvent.ShowSnackbar(
                    resourcesProvider.getString(R.string.error_send_data)
                )
            )
            updateForm { copy(button = button.copy(loading = false)) }
        }
    }

    override fun WriteMessageState.withForm(form: FormUi): WriteMessageState {
        return copy(form = form)
    }

    fun navigateToWebView(url: String, title: String) = viewModelScope.launch {
        sendEvent(WriteMessageEvent.GoToWebView(url, title))
    }

}