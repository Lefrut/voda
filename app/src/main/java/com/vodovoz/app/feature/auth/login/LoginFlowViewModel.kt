package com.vodovoz.app.feature.auth.login

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.R
import com.vodovoz.app.common.account.AccountManager
import com.vodovoz.app.common.agreement.AgreementController
import com.vodovoz.app.common.content.Event
import com.vodovoz.app.common.content.PagingContractViewModel
import com.vodovoz.app.common.content.State
import com.vodovoz.app.common.content.updateData
import com.vodovoz.app.common.resources.ResourcesProvider
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.design_system.model.updateButton
import com.vodovoz.app.design_system.model.widgets.FieldUi
import com.vodovoz.app.design_system.model.widgets.checkFields
import com.vodovoz.app.design_system.model.widgets.mapToUi
import com.vodovoz.app.design_system.model.widgets.updateFieldAndResetError
import com.vodovoz.app.domain.general.model.TooManyRequestsException
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.sitestate.SiteStateManager
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class LoginFlowViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val siteStateManager: SiteStateManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val resourcesProvider: ResourcesProvider,
) : PagingContractViewModel<LoginFlowViewModel.LoginState, LoginFlowViewModel.LoginEvents>(
    LoginState()
) {

    companion object {
        private const val AUTH_BUTTON = "sms"
        private const val NAVIGATION_BUTTON = "auth"
    }

    init {
        viewModelScope.launch {
            val settings = accountManager.fetchUserSettings()
            viewModelScope.launch {
                uiStateListener.value = state.copy(
                    data = state.data.copy(
                        settings = settings,
                    )
                )
            }
        }

        fetchLoginDetails()
    }

    fun fetchLoginDetails() = viewModelScope.launch {
        uiStateListener.updateData { s -> s.copy(uiState = LoginUiState.Loading) }

        val loginDetailsDeferred =
            async { vodovozServiceRepository.getLoginDetails().singleResult() }
        val loginDetailsResult = loginDetailsDeferred.await()

        val siteState = siteStateManager.siteStateSnapshot
        val agreementText = AgreementController.getText()
        val showRegisterText = siteState?.isSmsEnabled != true

        loginDetailsResult.onSuccess { loginDetails ->

            val buttons = loginDetails.buttons.map { colorfulButtonModel ->
                colorfulButtonModel.toUi()
            }.updateButton(AUTH_BUTTON) { it.copy(enabled = false) }

            uiStateListener.updateData { s ->
                s.copy(
                    description = loginDetails.description,
                    title = loginDetails.title,
                    buttons = buttons,
                    fields = loginDetails.fields.mapToUi(),
                    agreementTextHtml = agreementText,
                    uiState = LoginUiState.Success,
                    showAgreements = loginDetails.haveAgreement,
                    showRegisterText = showRegisterText,
                )
            }
        }

        if (siteState == null || loginDetailsResult.isFailure) {
            uiStateListener.updateData { s ->
                s.copy(uiState = LoginUiState.Error)
            }
        }

    }

    private fun requestCode() = viewModelScope.launch {
        val phoneField = dataState.fields.firstOrNull() ?: return@launch

        uiStateListener.updateData { s ->
            s.copy(
                buttons = s.buttons.updateButton(AUTH_BUTTON) { btn -> btn.copy(loading = true) }
            )
        }

        val requestPhoneCodeUrl = siteStateManager.siteStateFlow.value?.smsUrl?.takeIf { sms ->
            sms.isNotBlank()
        } ?: kotlin.run {
            uiStateListener.updateData { s ->
                s.copy(
                    buttons = s.buttons.updateButton(AUTH_BUTTON) { btn ->
                        btn.copy(loading = false)
                    },
                    errorText = resourcesProvider.getString(R.string.error_site_login)
                )
            }
            return@launch
        }


        val requestPhoneCodeResult = vodovozServiceRepository.requestPhoneCode(
            url = requestPhoneCodeUrl,
            phone = phoneField.value,
            newsletter = dataState.subscribeChecked
        ).singleResult()

        uiStateListener.updateData { s ->
            s.copy(
                buttons = s.buttons.updateButton(AUTH_BUTTON) { btn ->
                    btn.copy(loading = false)
                }
            )
        }

        requestPhoneCodeResult.onFailure { t ->
            when (t) {
                is TooManyRequestsException -> {
                    eventListener.emit(
                        LoginEvents.GoToLoginByPhone(
                            phoneField.value,
                            t.remainingSeconds
                        )
                    )
                }

                else -> {
                    uiStateListener.updateData { s ->
                        s.copy(errorText = resourcesProvider.getString(R.string.error_login))
                    }

                }
            }
        }.onSuccess {
            eventListener.emit(LoginEvents.GoToLoginByPhone(phoneField.value, it.waitSeconds))
        }
    }

    fun navigateBack() = viewModelScope.launch {
        eventListener.emit(LoginEvents.GoBack)
    }

    fun changeField(field: FieldUi, updatedField: FieldUi) = viewModelScope.launch {
        uiStateListener.updateData { s ->
            val updatedFields = s.fields.updateFieldAndResetError(field, updatedField)

            s.copy(
                fields = updatedFields,
                buttons = s.buttons.updateButton(AUTH_BUTTON) { button ->
                    button.copy(enabled = updatedFields.checkFields() && (s.agreementChecked || !s.showAgreements))
                },
                errorText = ""
            )
        }
    }

    fun openAgreementUrl(url: String, index: Int) = viewModelScope.launch {
        val title = AgreementController.getTitle(index) ?: ""
        eventListener.emit(LoginEvents.GoToWebView(url, title))
    }

    fun checkSubscribe(checked: Boolean) = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(subscribeChecked = checked)
        }
    }

    fun checkAgreement(checked: Boolean) = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(
                agreementChecked = checked,
                buttons = s.buttons.updateButton(AUTH_BUTTON) { button ->
                    button.copy(enabled = s.fields.checkFields() && checked)
                }
            )
        }

    }

    fun navigateToRegister() = viewModelScope.launch {
        eventListener.emit(LoginEvents.GoToRegister)
    }

    fun activateButton(button: ColorfulButtonUi) = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(errorText = "")
        }

        when (button.id) {
            NAVIGATION_BUTTON -> {
                eventListener.emit(LoginEvents.GoToLoginByEmail)
            }

            AUTH_BUTTON -> {
                requestCode()
            }

            else -> {

            }
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

        val showRegisterText: Boolean = false,
        val agreementTextHtml: String = "",
        val showAgreements: Boolean = false,
        val agreementChecked: Boolean = true,
        val subscribeChecked: Boolean = false,
        val title: String = "",
        val description: String = "",
        val fields: List<FieldUi> = emptyList(),
        val buttons: List<ColorfulButtonUi> = emptyList(),
        val errorText: String = "",
        val uiState: LoginUiState = LoginUiState.Loading,
    ) : State

    sealed interface LoginUiState {
        data object Success : LoginUiState
        data object Loading : LoginUiState
        data object Error : LoginUiState
    }
}