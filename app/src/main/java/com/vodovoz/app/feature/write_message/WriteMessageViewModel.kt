package com.vodovoz.app.feature.write_message

import androidx.lifecycle.viewModelScope
import com.vodovoz.app.R
import com.vodovoz.app.common.resources.ResourcesProvider
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.design_system.model.widgets.EmptyTextValidator
import com.vodovoz.app.design_system.model.widgets.FieldUi
import com.vodovoz.app.design_system.model.widgets.NoRequiredValidator
import com.vodovoz.app.design_system.model.widgets.checkFields
import com.vodovoz.app.design_system.model.widgets.getErrorText
import com.vodovoz.app.design_system.model.widgets.mapToUi
import com.vodovoz.app.design_system.model.widgets.updateFieldAndResetError
import com.vodovoz.app.design_system.model.widgets.vodovozValidators
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.write_message.model.WriteMessageEvent
import com.vodovoz.app.feature.write_message.model.WriteMessageState
import com.vodovoz.app.feature.write_message.model.WriteMessageUiState
import com.vodovoz.app.ui.mvi.MviViewModel
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WriteMessageViewModel @Inject constructor(
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val resourcesProvider: ResourcesProvider,
) : MviViewModel<WriteMessageState, WriteMessageEvent>(WriteMessageState()) {

    init {
        fetchWriteMessageDetails()
    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(WriteMessageEvent.GoBack)
    }

    fun fetchWriteMessageDetails() = viewModelScope.launch {
        _state.update { s ->
            s.copy(uiState = WriteMessageUiState.Loading)
        }

        vodovozServiceRepository.getWriteMessageDetails().singleResult().onSuccess { form ->
            _state.update { s ->
                s.copy(
                    uiState = WriteMessageUiState.Body,
                    fields = form.fields.mapToUi(),
                    button = form.button.toUi().copy(
                        enabled = false
                    ),
                    title = form.title,
                    description = form.description
                )
            }
        }.onFailure {
            _state.update { s ->
                s.copy(
                    uiState = WriteMessageUiState.Error,
                )
            }

        }
    }

    fun changeField(field: FieldUi, updatedField: FieldUi) = viewModelScope.launch {
        _state.update { s ->
            val updatedFields = s.fields.updateFieldAndResetError(
                field, updatedField
            )
            s.copy(
                fields = updatedFields,
                button = s.button.copy(
                    enabled = updatedFields.checkFields(
                        validators = listOf(
                            NoRequiredValidator,
                            EmptyTextValidator
                        )
                    )
                )
            )
        }
    }

    fun sendMessage() = viewModelScope.launch {
        stateSnapshot.fields.checkFields(
            putErrors = true,
            validators = vodovozValidators,
            getSupportingText = { field ->
                field.getErrorText { resId ->
                    resourcesProvider.getString(resId)
                }
            }
        ) { fields, isValid ->
            if (!isValid) {
                _state.update { s ->
                    s.copy(
                        fields = fields
                    )
                }
                return@launch
            }
        }

        vodovozServiceRepository.sendMessage(
            stateSnapshot.fields.associate { it.id to it.value() }
        ).singleResult().onSuccess { placeholder ->
            _state.update { s ->
                s.copy(
                    uiState = WriteMessageUiState.Success(placeholder.toUi()),
                    button = s.button.copy(loading = true)
                )
            }
        }.onFailure {
            sendEvent(
                WriteMessageEvent.ShowSnackbar(
                    resourcesProvider.getString(R.string.error_send_data)
                )
            )
            _state.update { s ->
                s.copy(button = s.button.copy(loading = false, enabled = false))
            }
        }
    }

}