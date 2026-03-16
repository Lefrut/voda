package com.m.vodovoz.feature.auth.login

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.R
import com.m.vodovoz.common.account.AccountManager
import com.m.vodovoz.common.model.GlobalAppExtraAgreement
import com.m.vodovoz.common.resources.ResourcesProvider
import com.m.vodovoz.core.navigation.AuthArgs
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.domain.general.model.exceptions.TooManyRequestsException
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.auth.login.api.LoginNavKey
import com.m.vodovoz.feature.auth.model.AbstractAuthViewModel
import com.m.vodovoz.feature.auth.model.AuthDetailsUi
import com.m.vodovoz.feature.auth.model.AuthState
import com.m.vodovoz.feature.auth.model.selectedAccountTypeId
import com.m.vodovoz.feature.auth.model.toUi
import com.m.vodovoz.feature.auth.model.withAccountTypeSelection
import com.m.vodovoz.feature.sitestate.SiteStateManager
import com.m.vodovoz.ui.mvi.Event
import com.m.vodovoz.ui.mvi.launchInViewModelScope
import com.m.vodovoz.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch


private const val AUTH_BUTTON = "sms"
private const val NAVIGATION_BUTTON = "auth"

@HiltViewModel(assistedFactory = LoginFlowViewModel.Factory::class)
@Stable
class LoginFlowViewModel @AssistedInject constructor(
    private val siteStateManager: SiteStateManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val resourcesProvider: ResourcesProvider,
    private val savedStateHandle: SavedStateHandle,
    @Assisted private val navKey: LoginNavKey?,
) : AbstractAuthViewModel<LoginFlowViewModel.LoginState, LoginFlowViewModel.LoginEvents>(
    LoginState(), AUTH_BUTTON
) {


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
                .withBlockingButton { button ->
                    button.copy(enabled = false)
                }
                .copy(
                    warning = GlobalAppExtraAgreement.html,
                    waringTitles = GlobalAppExtraAgreement.titles,
                )
                .withAccountTypeSelection(
                    selectedAccountTypeId = navKey?.accountTypeId ?: savedStateHandle[AuthArgs.ACCOUNT_TYPE_ID]
                )

            updateState { s ->
                s.copy(
                    authDetails = authDetails,
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

        val phoneField = fields.firstOrNull() ?: return@launch

        setBlockingButtonState(loading = true)

        val requestPhoneCodeUrl = siteStateManager.siteStateFlow.value?.smsUrl?.takeIf { sms ->
            sms.isNotBlank()
        } ?: kotlin.run {
            setBlockingButtonState(loading = false)
            setLastFieldError(resourcesProvider.getString(R.string.error_site_login))
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

        setBlockingButtonState(loading = false)

        requestPhoneCodeResult.onFailure { t ->
            when (t) {
                is TooManyRequestsException -> {
                    sendEvent(
                        LoginEvents.GoToLoginByPhone(
                            phoneField.value,
                            t.remainingSeconds,
                            stateSnapshot.userUrl
                        )
                    )
                }

                else -> {
                    setLastFieldError(resourcesProvider.getString(R.string.error_login))
                }
            }
        }.onSuccess { requestCodeModel ->
            sendEvent(
                LoginEvents.GoToLoginByPhone(
                    phoneField.value,
                    requestCodeModel.waitSeconds,
                    stateSnapshot.userUrl
                )
            )
        }
    }

    override fun onBackClick() {
        launchInViewModelScope {
            sendEvent(LoginEvents.GoBack)
        }
    }

    override fun clickButton(button: ColorfulButtonUi) {
        launchInViewModelScope {
            setLastFieldError("")

            when (button.id) {
                NAVIGATION_BUTTON -> {
                    sendEvent(
                        LoginEvents.GoToLoginByEmail(
                            selectedAccountTypeId = stateSnapshot.authDetails.selectedAccountTypeId()
                        )
                    )
                }

                AUTH_BUTTON -> {
                    requestCode()
                }

                else -> {

                }
            }
        }
    }

    override fun clickHyperlink(url: String, title: String) {
        launchInViewModelScope {
            sendEvent(LoginEvents.GoToWebView(url, title))
        }
    }

    fun setAccountTypeById(accountTypeId: String?) {
        updateAuthDetails { withAccountTypeSelection(accountTypeId) }
    }

    sealed class LoginEvents : Event {
        data object GoBack : LoginEvents()
        data class GoToLoginByEmail(val selectedAccountTypeId: String) : LoginEvents()
        data class GoToLoginByPhone(val phone: String, val waitSeconds: Int, val userUrl: String) :
            LoginEvents()

        data object GoToRegister : LoginEvents()

        data class GoToWebView(val url: String, val title: String) : LoginEvents()
    }

    @Immutable
    data class LoginState(
        val settings: AccountManager.UserSettings? = null,
        val uiState: LoginUiState = LoginUiState.Loading,
        override val authDetails: AuthDetailsUi = AuthDetailsUi.Empty,
    ) : AuthState<LoginState>(authDetails) {
        override fun withAuthDetails(authDetails: AuthDetailsUi) =
            copy(authDetails = authDetails)
    }

    @Stable
    sealed interface LoginUiState {
        data object Success : LoginUiState
        data object Loading : LoginUiState
        data object Error : LoginUiState
    }

    @AssistedFactory
    interface Factory {
        fun create(navKey: LoginNavKey?): LoginFlowViewModel
    }
}
