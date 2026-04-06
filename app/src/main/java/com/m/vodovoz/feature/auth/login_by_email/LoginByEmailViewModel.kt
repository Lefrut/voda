package com.m.vodovoz.feature.auth.login_by_email

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.R
import com.m.vodovoz.common.account.LoginManager
import com.m.vodovoz.common.resources.ResourcesProvider
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.model.updateButton
import com.m.vodovoz.design_system.model.widgets.updateField
import com.m.vodovoz.domain.general.model.exceptions.ValidationException
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.auth.login.composables.LoginByEmailUiState
import com.m.vodovoz.feature.auth.login.model.LoginByEmailEvent
import com.m.vodovoz.feature.auth.login.model.LoginByEmailState
import com.m.vodovoz.feature.auth.login_by_email.api.LoginByEmailNavKey
import com.m.vodovoz.feature.auth.model.AbstractAuthViewModel
import com.m.vodovoz.feature.auth.model.AuthDetailsUi
import com.m.vodovoz.feature.auth.model.authValidators
import com.m.vodovoz.feature.auth.model.selectedAccountTypeId
import com.m.vodovoz.feature.auth.model.toUi
import com.m.vodovoz.feature.auth.model.withAccountTypeSelection
import com.m.vodovoz.ui.mvi.launchInViewModelScope
import com.m.vodovoz.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.launch

private const val LOGIN_BY_EMAIL_BUTTON = "otpravka"
private const val NAVIGATION_BUTTON = "registr"
private val LOGIN_BY_EMAIL_VALIDATORS = AuthDetailsUi.authValidators()

@HiltViewModel(assistedFactory = LoginByEmailViewModel.Factory::class)
@Stable
class LoginByEmailViewModel @AssistedInject constructor(
    val tabManager: TabManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val resourcesProvider: ResourcesProvider,
    private val loginManager: LoginManager,
    @Assisted private val navKey: LoginByEmailNavKey,
) : AbstractAuthViewModel<LoginByEmailState, LoginByEmailEvent>(
    LoginByEmailState(),
    LOGIN_BY_EMAIL_BUTTON
) {

    override val blockingButtonValidators = LOGIN_BY_EMAIL_VALIDATORS

    init {
        fetchLoginByEmailDetails()
    }

    private fun loginByEmail() = viewModelScope.launch {
        setBlockingButtonState(loading = true)


        val loginByEmailResult = vodovozServiceRepository.loginByEmail(
            stateSnapshot.fields.associate {
                it.id to it.value()
            } + stateSnapshot.checkboxes.associate { it.id to it.value() }
        ).singleResult()


        loginByEmailResult.onSuccess { userAuthInfo ->

            loginManager.initializeUserSession(
                userId = userAuthInfo.userId,
                userToken = userAuthInfo.token,
                userUrl = stateSnapshot.userUrl
            )

            setBlockingButtonState(loading = false, enabled = false)

            sendEvent(LoginByEmailEvent.RefreshAll)

        }.onFailure { t ->
            val defaultMessage = resourcesProvider.getString(R.string.error_login)

            val errorMessage = if (t is ValidationException) {
                t.message ?: defaultMessage
            } else {
                defaultMessage
            }


            updateAuthDetails {
                val currentFields = fields
                val lastField = currentFields.lastOrNull()

                copy(
                    buttons = buttons.updateButton(LOGIN_BY_EMAIL_BUTTON) { button ->
                        button.copy(loading = false)
                    },
                    fields = lastField?.let { field ->
                        currentFields.updateField(
                            field,
                            field.copy(supportingText = errorMessage, isError = true)
                        )
                    } ?: currentFields
                )
            }
        }
    }

    fun fetchLoginByEmailDetails() = viewModelScope.launch {
        updateState { s -> s.copy(uiState = LoginByEmailUiState.Loading) }

        val loginByEmailResult = vodovozServiceRepository.getLoginByEmailDetails().singleResult()


        loginByEmailResult.onSuccess { loginDetails ->
            val authDetails = loginDetails.toUi().withBlockingButton { button ->
                button.copy(enabled = false)
            }.withAccountTypeSelection(
                navKey.accountTypeId
            )

            updateState { s ->
                s.copy(
                    authDetails = authDetails,
                    uiState = LoginByEmailUiState.Success,
                )
            }
        }.onFailure {
            updateState { s ->
                s.copy(uiState = LoginByEmailUiState.Error)
            }
        }
    }

    override fun clickButton(button: ColorfulButtonUi) {
        launchInViewModelScope {
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
    }

    private fun navigateToRegister() = viewModelScope.launch {
        sendEvent(LoginByEmailEvent.GoToRegister)
    }

    override fun clickHyperlink(url: String, title: String) {
        launchInViewModelScope {
            sendEvent(LoginByEmailEvent.GoToWebView(url, title))
        }
    }

    override fun onBackClick() {
        launchInViewModelScope {
            sendEvent(
                LoginByEmailEvent.GoBack(
                    selectedAccountTypeId = stateSnapshot.authDetails.selectedAccountTypeId()
                )
            )
        }
    }

    override fun clickForgotPassword() {
        launchInViewModelScope {
            sendEvent(LoginByEmailEvent.GoToRecoverPassword)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(navKey: LoginByEmailNavKey): LoginByEmailViewModel
    }
}
