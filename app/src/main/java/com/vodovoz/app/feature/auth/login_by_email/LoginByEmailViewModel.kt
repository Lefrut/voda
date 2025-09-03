package com.vodovoz.app.feature.auth.login_by_email

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.R
import com.vodovoz.app.common.account.LoginManager
import com.vodovoz.app.common.agreement.AgreementController
import com.vodovoz.app.common.resources.ResourcesProvider
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.design_system.model.updateButton
import com.vodovoz.app.design_system.model.widgets.CheckboxUi
import com.vodovoz.app.design_system.model.widgets.EmailValidator
import com.vodovoz.app.design_system.model.widgets.FieldUi
import com.vodovoz.app.design_system.model.widgets.MessageValidator
import com.vodovoz.app.design_system.model.widgets.NameValidator
import com.vodovoz.app.design_system.model.widgets.NoRequiredValidator
import com.vodovoz.app.design_system.model.widgets.checkFields
import com.vodovoz.app.design_system.model.widgets.updateCheckbox
import com.vodovoz.app.design_system.model.widgets.updateField
import com.vodovoz.app.design_system.model.widgets.updateFieldAndResetError
import com.vodovoz.app.domain.general.model.exceptions.ValidationException
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.auth.login.composables.LoginByEmailUiState
import com.vodovoz.app.feature.auth.login.model.LoginByEmailEvent
import com.vodovoz.app.feature.auth.login.model.LoginByEmailState
import com.vodovoz.app.feature.auth.model.AuthDetailsUi
import com.vodovoz.app.feature.auth.model.agreementIsCheckedWhenAvailable
import com.vodovoz.app.feature.auth.model.authValidators
import com.vodovoz.app.feature.auth.model.toUi
import com.vodovoz.app.feature.sitestate.SiteStateManager
import com.vodovoz.app.ui.mvi.MviViewModel
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
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
                    authDetails = loginDetails.toUi(AgreementController.getText()).copy(
                        buttons = buttons
                    ),
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
            val updatedFields = s.fields.updateFieldAndResetError(field, updatedField)

            s.copy(
                authDetails = s.authDetails.copy(
                    fields = updatedFields,
                    buttons = s.buttons.updateButton(LOGIN_BY_EMAIL_BUTTON) { button ->
                        button.copy(
                            enabled = updatedFields.checkFields(
                                validators = listOf(
                                    NoRequiredValidator,
                                    EmailValidator,
                                    NameValidator,
                                    MessageValidator
                                )
                            ) && s.checkboxes.agreementIsCheckedWhenAvailable()
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

    fun openAgreementUrl(url: String, titleIndex: Int) = viewModelScope.launch {
        val title = AgreementController.getTitle(titleIndex) ?: ""
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
                            ) && updatedCheckboxes.agreementIsCheckedWhenAvailable()
                        )
                    }
                )
            )
        }
    }


}