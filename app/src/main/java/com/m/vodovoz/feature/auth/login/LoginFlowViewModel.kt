package com.m.vodovoz.feature.auth.login

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.R
import com.m.vodovoz.common.account.AccountManager
import com.m.vodovoz.common.agreement.AgreementController
import com.m.vodovoz.common.model.GlobalAppExtraAgreement
import com.m.vodovoz.common.resources.ResourcesProvider
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.model.updateButton
import com.m.vodovoz.design_system.model.widgets.CheckboxUi
import com.m.vodovoz.design_system.model.widgets.FieldUi
import com.m.vodovoz.design_system.model.widgets.checkFields
import com.m.vodovoz.design_system.model.widgets.updateCheckbox
import com.m.vodovoz.design_system.model.widgets.updateField
import com.m.vodovoz.design_system.model.widgets.updateFieldAndResetError
import com.m.vodovoz.domain.general.model.exceptions.TooManyRequestsException
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.auth.model.AuthDetailsUi
import com.m.vodovoz.feature.auth.model.AuthState
import com.m.vodovoz.feature.auth.model.agreementIsCheckedWhenAvailable
import com.m.vodovoz.feature.auth.model.authValidators
import com.m.vodovoz.feature.auth.model.toUi
import com.m.vodovoz.feature.sitestate.SiteStateManager
import com.m.vodovoz.ui.mvi.Event
import com.m.vodovoz.ui.mvi.MviViewModel
import com.m.vodovoz.util.extensions.extractLinksFromHtml
import com.m.vodovoz.util.extensions.singleResult
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
        updateState { s -> s.copy(uiState = LoginUiState.Loading) }

        val loginDetailsDeferred =
            async { vodovozServiceRepository.getLoginDetails().singleResult() }
        val loginDetailsResult = loginDetailsDeferred.await()

        loginDetailsResult.onSuccess { loginDetails ->
            val authDetails = loginDetails.toUi()

            updateState { s ->
                s.copy(
                    authDetails = authDetails.copy(
                        buttons = authDetails.buttons.updateButton(AUTH_BUTTON) { button ->
                            button.copy(enabled = false)
                        },
                        warning = GlobalAppExtraAgreement.html,
                        waringTitles = GlobalAppExtraAgreement.titles
                    ),
                    uiState = LoginUiState.Success,
                )
            }
        }.onFailure {
            updateState { s ->
                s.copy(uiState = LoginUiState.Error)
            }
        }
    }

    private fun requestCode() = viewModelScope.launch {
        val fields = stateSnapshot.authDetails.fields
        val buttons = stateSnapshot.authDetails.buttons

        val phoneField = fields.firstOrNull() ?: return@launch

        updateState { s ->
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
            updateState { s ->
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

        updateState { s ->
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
                    updateState { s ->
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
        updateState { s ->
            val authDetails = s.authDetails
            val updatedFields = s.fields.updateFieldAndResetError(field, updatedField)
            s.copy(
                authDetails = authDetails.copy(
                    fields = updatedFields,
                    buttons = s.buttons.updateButton(AUTH_BUTTON) { button ->
                        button.copy(
                            enabled = updatedFields.checkFields()
                                    && s.checkboxes.agreementIsCheckedWhenAvailable(authDetails.agreementCheckboxId)
                        )
                    }
                )
            )
        }

        updateState { s ->
            s.copy(
                authDetails = s.authDetails.copy(
                    fields = updateFieldsErrorText("")
                )
            )
        }

    }

    fun openUrl(url: String, title: String) = viewModelScope.launch {
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
        updateState { s ->
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
        updateState { s ->
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
                            ) && updatedCheckboxes.agreementIsCheckedWhenAvailable(authDetails.agreementCheckboxId)
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

    @Stable
    sealed interface LoginUiState {
        data object Success : LoginUiState
        data object Loading : LoginUiState
        data object Error : LoginUiState
    }
}