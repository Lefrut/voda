package com.m.vodovoz.feature.auth.login_by_email

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.R
import com.m.vodovoz.common.account.LoginManager
import com.m.vodovoz.common.resources.ResourcesProvider
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.design_system.model.updateButton
import com.m.vodovoz.design_system.model.widgets.CheckboxUi
import com.m.vodovoz.design_system.model.widgets.FieldUi
import com.m.vodovoz.design_system.model.widgets.checkFields
import com.m.vodovoz.design_system.model.widgets.updateCheckbox
import com.m.vodovoz.design_system.model.widgets.updateField
import com.m.vodovoz.design_system.model.widgets.updateFieldAndResetError
import com.m.vodovoz.design_system.model.widgets.vodovozValidators
import com.m.vodovoz.domain.general.model.exceptions.ValidationException
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.auth.login.composables.LoginByEmailUiState
import com.m.vodovoz.feature.auth.login.model.LoginByEmailEvent
import com.m.vodovoz.feature.auth.login.model.LoginByEmailState
import com.m.vodovoz.feature.auth.model.AuthDetailsUi
import com.m.vodovoz.feature.auth.model.agreementIsCheckedWhenAvailable
import com.m.vodovoz.feature.auth.model.authValidators
import com.m.vodovoz.feature.auth.model.toUi
import com.m.vodovoz.ui.mvi.MviViewModel
import com.m.vodovoz.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class LoginByEmailViewModel @Inject constructor(
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val resourcesProvider: ResourcesProvider,
    private val loginManager: LoginManager,
) : MviViewModel<LoginByEmailState, LoginByEmailEvent>(LoginByEmailState()) {

    companion object {
        private const val LOGIN_BY_EMAIL_BUTTON = "otpravka"
        private const val NAVIGATION_BUTTON = "registr"
    }

    init {
        fetchLoginByEmailDetails()
    }


    fun navigateBack() = viewModelScope.launch {
        sendEvent(LoginByEmailEvent.GoBack)
    }

    private fun loginByEmail() = viewModelScope.launch {
        updateState { s ->
            s.copy(
                authDetails = s.authDetails.copy(
                    buttons = s.buttons.updateButton(LOGIN_BY_EMAIL_BUTTON) { btn ->
                        btn.copy(loading = true)
                    }
                ),
            )
        }


        val loginByEmailResult = vodovozServiceRepository.loginByEmail(
            stateSnapshot.fields.associate {
                it.id to it.value()
            } + stateSnapshot.checkboxes.associate { it.id to it.value() }
        ).singleResult()


        loginByEmailResult.onSuccess { userAuthInfo ->

            loginManager.initializeUserSession(
                userAuthInfo.userId,
                userAuthInfo.token
            )

            updateState { s ->
                s.copy(
                    authDetails = s.authDetails.copy(
                        buttons = s.buttons.updateButton(LOGIN_BY_EMAIL_BUTTON) { btn ->
                            btn.copy(
                                loading = false,
                                enabled = false
                            )
                        }

                    )
                )
            }

            sendEvent(LoginByEmailEvent.RefreshAll)

        }.onFailure { t ->
            val defaultMessage = resourcesProvider.getString(R.string.error_login)

            val errorMessage = if (t is ValidationException) {
                t.message ?: defaultMessage
            } else {
                defaultMessage
            }


            updateState { s ->

                val lastField = s.fields.lastOrNull()

                s.copy(
                    authDetails = s.authDetails.copy(
                        buttons = s.buttons.updateButton(LOGIN_BY_EMAIL_BUTTON) { btn ->
                            btn.copy(loading = false)
                        },
                        fields = lastField?.let { field ->
                            s.fields.updateField(
                                field,
                                field.copy(supportingText = errorMessage, isError = true)
                            )
                        } ?: s.fields
                    )
                )
            }

        }


    }

    fun fetchLoginByEmailDetails() = viewModelScope.launch {
        updateState { s -> s.copy(uiState = LoginByEmailUiState.Loading) }

        val loginByEmailResult = vodovozServiceRepository.getLoginByEmailDetails().singleResult()


        loginByEmailResult.onSuccess { loginDetails ->
            val buttons = loginDetails.buttons.map { colorfulButtonModel ->
                colorfulButtonModel.toUi()
            }.updateButton(LOGIN_BY_EMAIL_BUTTON) { it.copy(enabled = false) }

            updateState { s ->
                s.copy(
                    authDetails = loginDetails.toUi().copy(buttons = buttons),
                    uiState = LoginByEmailUiState.Success,
                )
            }
        }.onFailure {
            updateState { s ->
                s.copy(uiState = LoginByEmailUiState.Error)
            }
        }
    }

    fun changeField(field: FieldUi, updatedField: FieldUi) = viewModelScope.launch {

        updateState { s ->
            val authDetails = s.authDetails
            val updatedFields = s.fields.updateFieldAndResetError(field, updatedField)

            s.copy(
                authDetails = authDetails.copy(
                    fields = updatedFields,
                    buttons = authDetails.buttons.updateButton(LOGIN_BY_EMAIL_BUTTON) { button ->
                        button.copy(
                            enabled = updatedFields.checkFields(
                                validators = vodovozValidators
                            ) && s.checkboxes.agreementIsCheckedWhenAvailable(authDetails.agreementCheckboxId)
                        )
                    }
                ),

                )
        }
    }

    fun activateButton(button: ColorfulButtonUi) = viewModelScope.launch {
        when (button.id) {
            LOGIN_BY_EMAIL_BUTTON -> {
                loginByEmail()
            }

            NAVIGATION_BUTTON -> {
                navigateToRegister()
            }

            else -> {}
        }
    }

    private fun navigateToRegister() = viewModelScope.launch {
        sendEvent(LoginByEmailEvent.GoToRegister)
    }

    fun openUrl(url: String, title: String) = viewModelScope.launch {
        sendEvent(LoginByEmailEvent.GoToWebView(url, title))
    }

    fun navigateToRecoveryPassword() = viewModelScope.launch {
        sendEvent(LoginByEmailEvent.GoToRecoverPassword)
    }


    fun changeCheckbox(checkbox: CheckboxUi, updatedCheckbox: CheckboxUi) {
        updateState { s ->
            val authDetails = s.authDetails
            val updatedCheckboxes = authDetails.checkboxes.updateCheckbox(
                checkbox, updatedCheckbox
            )

            s.copy(
                authDetails = authDetails.copy(
                    checkboxes = updatedCheckboxes,
                    buttons = authDetails.buttons.updateButton(LOGIN_BY_EMAIL_BUTTON) { button ->
                        button.copy(
                            enabled = authDetails.fields.checkFields(
                                validators = AuthDetailsUi.authValidators()
                            ) && updatedCheckboxes.agreementIsCheckedWhenAvailable(authDetails.agreementCheckboxId)
                        )
                    }
                )
            )
        }
    }


}