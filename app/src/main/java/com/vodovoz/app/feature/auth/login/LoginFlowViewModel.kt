package com.vodovoz.app.feature.auth.login

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.R
import com.vodovoz.app.common.account.AccountManager
import com.vodovoz.app.common.agreement.AgreementController
import com.vodovoz.app.ui.mvi.Event
import com.vodovoz.app.ui.mvi.MviViewModel
import kotlinx.coroutines.flow.update
import com.vodovoz.app.common.resources.ResourcesProvider
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.updateButton
import com.vodovoz.app.design_system.model.widgets.CheckboxUi
import com.vodovoz.app.design_system.model.widgets.FieldUi
import com.vodovoz.app.design_system.model.widgets.checkFields
import com.vodovoz.app.design_system.model.widgets.updateCheckbox
import com.vodovoz.app.design_system.model.widgets.updateField
import com.vodovoz.app.design_system.model.widgets.updateFieldAndResetError
import com.vodovoz.app.domain.general.model.TooManyRequestsException
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.auth.model.AuthDetailsUi
import com.vodovoz.app.feature.auth.model.AuthState
import com.vodovoz.app.feature.auth.model.agreementIsCheckedWhenAvailable
import com.vodovoz.app.feature.auth.model.authValidators
import com.vodovoz.app.feature.auth.model.toUi
import com.vodovoz.app.feature.sitestate.SiteStateManager
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class LoginFlowViewModel @Inject constructor(
    private val siteStateManager: SiteStateManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val resourcesProvider: ResourcesProvider,
) : MviViewModel<LoginFlowViewModel.LoginState, LoginFlowViewModel.LoginEvents>(
    LoginState()
) {

    companion object {
        private const val AUTH_BUTTON = "sms"
        private const val NAVIGATION_BUTTON = "auth"
    }

    init {
        fetchLoginDetails()
    }

    fun fetchLoginDetails() = viewModelScope.launch {
        _state.update { s -> s.copy(uiState = LoginUiState.Loading) }

        val loginDetailsDeferred =
            async { vodovozServiceRepository.getLoginDetails().singleResult() }
        val loginDetailsResult = loginDetailsDeferred.await()

        loginDetailsResult.onSuccess { loginDetails ->
            val authDetails = loginDetails.toUi(AgreementController.getText())

            _state.update { s ->
                s.copy(
                    authDetails = authDetails.copy(
                        buttons = authDetails.buttons.updateButton(AUTH_BUTTON) { button ->
                            button.copy(enabled = false)
                        }
                    ),
                    uiState = LoginUiState.Success,
                )
            }
        }.onFailure {
            _state.update { s ->
                s.copy(uiState = LoginUiState.Error)
            }
        }
    }

    private fun requestCode() = viewModelScope.launch {
        val fields = stateSnapshot.authDetails.fields
        val buttons = stateSnapshot.authDetails.buttons

        val phoneField = fields.firstOrNull() ?: return@launch

        _state.update { s ->
            s.copy(
                authDetails = s.authDetails.copy(
                    buttons = buttons.updateButton(AUTH_BUTTON) { btn ->
                        btn.copy(loading = true)
                    }
                ),
            )
        }

        val requestPhoneCodeUrl = siteStateManager.siteStateFlow.value?.smsUrl?.takeIf { sms ->
            sms.isNotBlank()
        } ?: kotlin.run {
            val lastField = fields.lastOrNull()
            _state.update { s ->
                s.copy(
                    authDetails = s.authDetails.copy(
                        buttons = buttons.updateButton(AUTH_BUTTON) { btn ->
                            btn.copy(loading = false)
                        },
                        fields = lastField?.let {
                            fields.updateField(
                                lastField, lastField.copy(
                                    supportingText = resourcesProvider.getString(R.string.error_site_login),
                                    isError = true
                                )
                            )
                        } ?: fields
                    ),
                )
            }
            return@launch
        }


        val requestPhoneCodeResult = vodovozServiceRepository.requestPhoneCode(
            url = requestPhoneCodeUrl,
            phone = phoneField.value,
            params = stateSnapshot.checkboxes.associate { checkbox ->
                checkbox.id to checkbox.value()
            } + stateSnapshot.fields.associate { field ->
                field.id to field.value()
            }
        ).singleResult()

        _state.update { s ->
            s.copy(
                authDetails = s.authDetails.copy(
                    buttons = buttons.updateButton(AUTH_BUTTON) { btn ->
                        btn.copy(loading = false)
                    }
                )
            )
        }

        requestPhoneCodeResult.onFailure { t ->
            when (t) {
                is TooManyRequestsException -> {
                    sendEvent(
                        LoginEvents.GoToLoginByPhone(
                            phoneField.value,
                            t.remainingSeconds
                        )
                    )
                }

                else -> {
                    _state.update { s ->
                        s.copy(
                            authDetails = s.authDetails.copy(
                                fields = updateFieldsErrorText(
                                    resourcesProvider.getString(R.string.error_login)
                                )
                            )
                        )
                    }

                }
            }
        }.onSuccess {
            sendEvent(LoginEvents.GoToLoginByPhone(phoneField.value, it.waitSeconds))
        }
    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(LoginEvents.GoBack)
    }

    fun changeField(field: FieldUi, updatedField: FieldUi) = viewModelScope.launch {


        _state.update { s ->

            val updatedFields = s.fields.updateFieldAndResetError(field, updatedField)

            s.copy(
                authDetails = s.authDetails.copy(
                    fields = updatedFields,
                    buttons = s.buttons.updateButton(AUTH_BUTTON) { button ->
                        button.copy(
                            enabled = updatedFields.checkFields()
                                    && s.checkboxes.agreementIsCheckedWhenAvailable()
                        )
                    }
                ),
            )


        }

        _state.update { s ->
            s.copy(
                authDetails = s.authDetails.copy(
                    fields = updateFieldsErrorText("")
                )
            )
        }

    }

    fun openAgreementUrl(url: String, index: Int) = viewModelScope.launch {
        val title = AgreementController.getTitle(index) ?: ""
        sendEvent(LoginEvents.GoToWebView(url, title))
    }

    private fun updateFieldsErrorText(errorText: String): List<FieldUi> {
        val authDetails = stateSnapshot.authDetails
        val fields = authDetails.fields
        val lastField = fields.lastOrNull()

        return lastField?.let {
            fields.updateField(
                lastField, lastField.copy(
                    supportingText = errorText,
                    isError = errorText.isNotBlank()
                )
            )
        } ?: fields
    }

    fun activateButton(button: ColorfulButtonUi) = viewModelScope.launch {
        _state.update { s ->
            s.copy(
                authDetails = s.authDetails.copy(
                    fields = updateFieldsErrorText("")
                )
            )
        }

        when (button.id) {
            NAVIGATION_BUTTON -> {
                sendEvent(LoginEvents.GoToLoginByEmail)
            }

            AUTH_BUTTON -> {
                requestCode()
            }

            else -> {

            }
        }
    }

    fun changeCheckbox(checkbox: CheckboxUi, updatedCheckbox: CheckboxUi) {
        _state.update { s ->
            val authDetails = s.authDetails
            val updatedCheckboxes = authDetails.checkboxes.updateCheckbox(
                checkbox, updatedCheckbox
            )

            s.copy(
                authDetails = authDetails.copy(
                    checkboxes = updatedCheckboxes,
                    buttons = authDetails.buttons.updateButton(AUTH_BUTTON) { button ->
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

    sealed class LoginEvents : Event {
        data object AuthSuccess : LoginEvents()
        data object GoBack : LoginEvents()
        data object GoToLoginByEmail : LoginEvents()
        data class GoToLoginByPhone(val phone: String, val waitSeconds: Int) : LoginEvents()
        data object GoToRegister : LoginEvents()

        data class GoToWebView(val url: String, val title: String) : LoginEvents()
    }

    @Immutable
    data class LoginState(
        val settings: AccountManager.UserSettings? = null,
        val uiState: LoginUiState = LoginUiState.Loading,
        override val authDetails: AuthDetailsUi = AuthDetailsUi.Empty,
    ) : AuthState(authDetails)

    sealed interface LoginUiState {
        data object Success : LoginUiState
        data object Loading : LoginUiState
        data object Error : LoginUiState
    }
}