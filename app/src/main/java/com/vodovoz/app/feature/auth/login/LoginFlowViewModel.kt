package com.vodovoz.app.feature.auth.login

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.R
import com.vodovoz.app.common.account.data.AccountManager
import com.vodovoz.app.common.account.data.LoginManager
import com.vodovoz.app.common.agreement.AgreementController
import com.vodovoz.app.common.content.Event
import com.vodovoz.app.common.content.PagingContractViewModel
import com.vodovoz.app.common.content.State
import com.vodovoz.app.common.content.toErrorState
import com.vodovoz.app.common.content.updateData
import com.vodovoz.app.common.like.LikeManager
import com.vodovoz.app.common.resources.ResourcesProvider
import com.vodovoz.app.common.token.FirebaseTokenManager
import com.vodovoz.app.data.MainRepository
import com.vodovoz.app.data.model.common.ResponseEntity
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.design_system.model.updateButton
import com.vodovoz.app.domain.general.model.TooManyRequestsException
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.preorder.model.FieldUi
import com.vodovoz.app.feature.preorder.model.checkFields
import com.vodovoz.app.feature.preorder.model.mapToUi
import com.vodovoz.app.feature.preorder.model.updateFieldAndResetError
import com.vodovoz.app.feature.sitestate.SiteStateManager
import com.vodovoz.app.ui.model.enum.AuthType
import com.vodovoz.app.util.extensions.debugLog
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginFlowViewModel @Inject constructor(
    private val repository: MainRepository,
    private val accountManager: AccountManager,
    private val firebaseTokenManager: FirebaseTokenManager,
    private val loginManager: LoginManager,
    private val siteStateManager: SiteStateManager,
    private val likeManager: LikeManager,
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
            val phone = loginManager.fetchLastAuthPhone()
            viewModelScope.launch {
                uiStateListener.value = state.copy(
                    data = state.data.copy(
                        settings = settings,
                        lastAuthPhone = phone
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
        val siteState = siteStateManager.requestSiteState()
        val loginDetailsResult = loginDetailsDeferred.await()

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
                buttons = s.buttons.updateButton(AUTH_BUTTON) { it.copy(loading = true) }
            )
        }

        val requestPhoneCodeUrl = siteStateManager.siteStateFlow.value?.smsUrl?.takeIf {
            it.isNotBlank()
        } ?: kotlin.run {
            siteStateManager.requestSiteState()
            uiStateListener.updateData { s ->
                s.copy(
                    buttons = s.buttons.updateButton(AUTH_BUTTON) { it.copy(loading = false) },
                    errorText = resourcesProvider.getString(R.string.error_site_login)
                )
            }
            return@launch
        }


        val requestPhoneCodeResult = vodovozServiceRepository.requestPhoneCode(
            requestPhoneCodeUrl, phoneField.value
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

    fun authByEmail(email: String, password: String) {

        uiStateListener.value = state.copy(
            loadingPage = true, data = state.data.copy(
                settings = AccountManager.UserSettings(
                    email = email,
                    password = password
                )
            )
        )
        viewModelScope.launch {
            flow { emit(repository.authByEmail(email, password)) }
                .onEach { response ->
                    when (response) {
                        is ResponseEntity.Hide -> {}
                        is ResponseEntity.Success -> {
                            accountManager.updateLastLoginSetting(
                                AccountManager.UserSettings(
                                    email = email,
                                    password = password
                                )
                            )
                            accountManager.updateUserId(response.data.id)
                            accountManager.updateUserToken(response.data.token)
                            likeManager.updateLikesAfterLogin(response.data.id)

                            uiStateListener.value =
                                state.copy(error = null, loadingPage = false)
                            clearData()
                            eventListener.emit(LoginEvents.AuthSuccess)
                            firebaseTokenManager.sendFirebaseToken()
                        }

                        is ResponseEntity.Error -> {
                            uiStateListener.value =
                                state.copy(loadingPage = false)

                        }
                    }

                }
                .flowOn(Dispatchers.Default)
                .catch {
                    debugLog { "auth by email error ${it.localizedMessage}" }
                    uiStateListener.value =
                        state.copy(error = it.toErrorState(), loadingPage = false)
                }
                .collect()
        }
    }


    private fun clearData() {
        loginManager.updateLastRequestCodeDate(0)
        loginManager.updateLastRequestCodeTimeOut(0)
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
        val authType: AuthType = AuthType.PHONE,
        val requestUrl: String? = null,
        val settings: AccountManager.UserSettings? = null,
        val lastAuthPhone: String? = null,
        val showPassword: Boolean = false,


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