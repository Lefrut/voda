package com.m.vodovoz.feature.auth.recover_password

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.R
import com.m.vodovoz.common.resources.ResourcesProvider
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.design_system.model.updateButton
import com.m.vodovoz.design_system.model.widgets.CheckboxUi
import com.m.vodovoz.design_system.model.widgets.FieldUi
import com.m.vodovoz.design_system.model.widgets.SwitchUi
import com.m.vodovoz.design_system.model.widgets.mapToDomain
import com.m.vodovoz.design_system.model.widgets.updateCheckbox
import com.m.vodovoz.design_system.model.widgets.updateField
import com.m.vodovoz.design_system.model.widgets.updateFieldAndResetError
import com.m.vodovoz.domain.general.model.exceptions.RequestException
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.auth.model.AbstractAuthViewModel
import com.m.vodovoz.feature.auth.model.toUi
import com.m.vodovoz.feature.auth.recover_password.model.RecoverPasswordEvent
import com.m.vodovoz.feature.auth.recover_password.model.RecoverPasswordState
import com.m.vodovoz.feature.auth.recover_password.model.RecoverPasswordUiState
import com.m.vodovoz.ui.mvi.launchInViewModelScope
import com.m.vodovoz.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val RECOVER_PASSWORD_BUTTON = "otpravka"

@Stable
@HiltViewModel
class RecoverPasswordViewModel @Inject constructor(
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val resourcesProvider: ResourcesProvider,
) : AbstractAuthViewModel<RecoverPasswordState, RecoverPasswordEvent>(
    RecoverPasswordState(),
    RECOVER_PASSWORD_BUTTON
) {

    init {
        fetchRecoverPasswordDetails()
    }

    override suspend fun listenAuthDetailsChanges() {
        // Keep original screen behavior: button state is controlled in change handlers.
    }

    override fun clickButton(button: ColorfulButtonUi) {
        launchInViewModelScope {
            when (button.id) {
                RECOVER_PASSWORD_BUTTON -> {
                    recoverPassword()
                }

                else -> Unit
            }
        }
    }

    fun fetchRecoverPasswordDetails() = viewModelScope.launch {
        updateState { s ->
            s.copy(uiState = RecoverPasswordUiState.Loading)
        }

        val recoverPasswordDeferred =
            async { vodovozServiceRepository.getRecoverPasswordDetails().singleResult() }


        val recoverPasswordDetailsResult = recoverPasswordDeferred.await()

        recoverPasswordDetailsResult.onSuccess { recoverPasswordDetails ->
            val authDetails = recoverPasswordDetails.toUi().withBlockingButton { button ->
                button.copy(enabled = false)
            }
            updateState { s ->
                s.copy(
                    uiState = RecoverPasswordUiState.Body,
                    authDetails = authDetails.copy(
                        showForgotPassword = true
                    )
                )
            }
        }.onFailure {
            updateState { s ->
                s.copy(uiState = RecoverPasswordUiState.Error)
            }
        }
    }

    private fun recoverPassword() = viewModelScope.launch {
        setBlockingButtonState(loading = true)


        val recoverPasswordResult = vodovozServiceRepository.recoverPassword(
            stateSnapshot.fields.mapToDomain()
        ).singleResult()

        recoverPasswordResult.onFailure { t ->
            val errorMessage = when (t) {
                is RequestException -> {
                    t.message ?: resourcesProvider.getString(R.string.error_send_data)
                }

                else -> {
                    resourcesProvider.getString(R.string.error_send_data)
                }
            }


            updateAuthDetails {
                val field = fields.lastOrNull() ?: return@updateAuthDetails this

                copy(
                    fields = fields.updateField(
                        field,
                        field.copy(
                            isError = true,
                            supportingText = errorMessage
                        )
                    ),
                    buttons = buttons.updateButton(RECOVER_PASSWORD_BUTTON) { button ->
                        button.copy(loading = false)
                    }
                )
            }


        }.onSuccess { placeholder ->
            updateState { s ->
                s.copy(
                    uiState = RecoverPasswordUiState.Success(placeholder.toUi()),
                    authDetails = s.authDetails.withBlockingButton { button ->
                        button.copy(loading = false)
                    }

                )
            }
        }

    }

    override fun changeField(field: FieldUi, updatedField: FieldUi) {
        launchInViewModelScope {
            updateAuthDetails {
                val updatedFields = fields.updateFieldAndResetError(field, updatedField)

                copy(
                    fields = updatedFields,
                    buttons = buttons.updateButton(RECOVER_PASSWORD_BUTTON) { button ->
                        button.copy(
                            enabled = isBlockingButtonEnabled(fields = updatedFields)
                        )
                    }
                )
            }
        }
    }

    override fun clickHyperlink(url: String, title: String) {
        launchInViewModelScope {
            sendEvent(RecoverPasswordEvent.GoToWebView(url, title))
        }
    }

    override fun changeCheckbox(checkbox: CheckboxUi, updatedCheckbox: CheckboxUi) {
        launchInViewModelScope {
            updateAuthDetails {
                val updatedCheckboxes = checkboxes.updateCheckbox(
                    checkbox, updatedCheckbox
                )

                copy(
                    checkboxes = updatedCheckboxes,
                    buttons = buttons.updateButton(RECOVER_PASSWORD_BUTTON) { button ->
                        button.copy(
                            enabled = isBlockingButtonEnabled(checkboxes = updatedCheckboxes)
                        )
                    }
                )
            }
        }
    }

    override fun onBackClick() {
        launchInViewModelScope {
            sendEvent(RecoverPasswordEvent.GoBack)
        }
    }

    fun navigateBack() = onBackClick()

    override fun changeSwitch(switch: SwitchUi, updatedSwitchUi: SwitchUi) = Unit

}
