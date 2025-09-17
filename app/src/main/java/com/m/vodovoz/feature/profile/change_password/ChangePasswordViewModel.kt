package com.m.vodovoz.feature.profile.change_password

import androidx.lifecycle.viewModelScope
import com.m.vodovoz.R
import com.m.vodovoz.common.resources.ResourcesProvider
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.design_system.model.widgets.FieldUi
import com.m.vodovoz.design_system.model.widgets.NoRequiredValidator
import com.m.vodovoz.design_system.model.widgets.checkFields
import com.m.vodovoz.design_system.model.widgets.mapToDomain
import com.m.vodovoz.design_system.model.widgets.toUi
import com.m.vodovoz.design_system.model.widgets.updateFieldAndResetError
import com.m.vodovoz.domain.general.model.exceptions.ValidationException
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.profile.change_password.model.ChangePasswordEvent
import com.m.vodovoz.feature.profile.change_password.model.ChangePasswordState
import com.m.vodovoz.feature.profile.change_password.model.ChangePasswordUiState
import com.m.vodovoz.ui.mvi.MviViewModel
import com.m.vodovoz.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val resourcesProvider: ResourcesProvider,
) : MviViewModel<ChangePasswordState, ChangePasswordEvent>(
    ChangePasswordState()
) {
    init {
        viewModelScope.launch { delay(200) }.invokeOnCompletion {
            fetchChangePasswordDetails()
        }
    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(ChangePasswordEvent.GoBack)
    }


    private fun fetchChangePasswordDetails() = viewModelScope.launch {
        updateState { s ->
            s.copy(uiState = ChangePasswordUiState.Loading)
        }
        val changePasswordDetailsResult =
            vodovozServiceRepository.getChangePasswordDetails().singleResult()

        changePasswordDetailsResult.onSuccess { model ->
            updateState { s ->
                s.copy(
                    fields = model.fields.map { field -> field.toUi() },
                    title = model.title,
                    uiState = ChangePasswordUiState.ChangePassword
                )
            }
        }.onFailure {
            sendEvent(ChangePasswordEvent.GoBack)
        }
    }

    fun changeField(field: FieldUi, updatedField: FieldUi) = viewModelScope.launch {
        updateState { s ->
            val updatedFields = s.fields.updateFieldAndResetError(field, updatedField)
            s.copy(
                fields = updatedFields,
                buttonEnabled = updatedFields.checkFields(validators = listOf(NoRequiredValidator))
            )
        }
    }

    fun updatePassword() = viewModelScope.launch {

        val fields = stateSnapshot.fields.mapToDomain()
        val passwordField = fields.firstOrNull() ?: return@launch

        updateState { s ->
            s.copy(buttonLoading = true)
        }

        val updatePasswordResult =
            vodovozServiceRepository.updatePassword(passwordField.value).singleResult()
        updatePasswordResult.onSuccess {
            updateState { s ->
                s.copy(uiState = ChangePasswordUiState.Placeholder(it.toUi()))
            }
        }.onFailure { t ->

            val message = when (t) {
                is ValidationException -> {
                    t.message?.ifBlank {
                        resourcesProvider.getString(R.string.error_password_unknown)
                    } ?: resourcesProvider.getString(R.string.error_password_unknown)
                }

                else -> {
                    resourcesProvider.getString(R.string.error_password_unknown)
                }
            }

            sendEvent(
                ChangePasswordEvent.ShowSnackbar(message)
            )
        }
        updateState { s -> s.copy(buttonLoading = false, buttonEnabled = false) }
    }
}