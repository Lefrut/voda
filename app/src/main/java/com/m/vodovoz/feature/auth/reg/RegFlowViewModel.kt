package com.m.vodovoz.feature.auth.reg

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.R
import com.m.vodovoz.common.account.AccountManager
import com.m.vodovoz.common.account.LoginManager
import com.m.vodovoz.common.resources.ResourcesProvider
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.model.mapToUi
import com.m.vodovoz.design_system.model.updateButton
import com.m.vodovoz.design_system.model.widgets.checkFields
import com.m.vodovoz.design_system.model.widgets.getErrorText
import com.m.vodovoz.domain.general.model.exceptions.ValidationException
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.auth.model.AbstractAuthViewModel
import com.m.vodovoz.feature.auth.model.AuthDetailsUi
import com.m.vodovoz.feature.auth.model.AuthState
import com.m.vodovoz.feature.auth.model.toUi
import com.m.vodovoz.ui.mvi.Event
import com.m.vodovoz.ui.mvi.launchInViewModelScope
import com.m.vodovoz.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val REGISTER_BUTTON = "otpravka"
private const val NAVIGATION_BUTTON = "auth"

@HiltViewModel
@Stable
class RegFlowViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val resourceProvider: ResourcesProvider,
    private val loginManager: LoginManager,
) : AbstractAuthViewModel<RegFlowViewModel.RegState, RegFlowViewModel.RegEvents>(
    RegState(),
    REGISTER_BUTTON
) {

    init {
        fetchRegisterDetails()
    }

    private fun fetchRegisterDetails() = viewModelScope.launch {
        updateState { s -> s.copy(uiState = RegUiState.Loading) }

        val registerFieldsResult =
            vodovozServiceRepository.getRegisterDetails().singleResult()

        registerFieldsResult.onSuccess { registerDetails ->
            updateState { s ->
                s.copy(
                    uiState = RegUiState.Success,
                    authDetails = registerDetails.toUi().copy(
                        buttons = registerDetails.buttons.mapToUi()
                            .updateButton(REGISTER_BUTTON) { btn ->
                                btn.copy(enabled = false)
                            }

                    )
                )
            }
        }.onFailure {
            updateState { s -> s.copy(uiState = RegUiState.Error) }
        }
    }

    private fun register() = viewModelScope.launch {
        val isValid = stateSnapshot.fields.checkFields(
            putErrors = true,
            getSupportingText = { field -> field.getErrorText { id -> resourceProvider.getString(id) } }
        ) { updatedFields, _ ->
            updateState { s ->
                s.withAuthDetails(
                    authDetails = s.authDetails.copy(
                        fields = updatedFields,
                        buttons = s.buttons.updateButton(REGISTER_BUTTON) { btn ->
                            btn.copy(enabled = false)
                        }
                    )
                )
            }
        }

        if (!isValid) return@launch



        updateState { s ->
            s.withAuthDetails(
                authDetails = s.authDetails.copy(
                    buttons = s.buttons.updateButton(REGISTER_BUTTON) { btn ->
                        btn.copy(loading = true)
                    }

                )
            )
        }

        val failMessage = resourceProvider.getString(R.string.error_registration)

        val registerResult = vodovozServiceRepository.register(
            stateSnapshot.fields.associate {
                it.id to it.value()
            } + stateSnapshot.fields.associate { it.id to it.value() }
        ).singleResult()

        registerResult.onSuccess { authInfo ->

            val email = stateSnapshot.fields.firstOrNull { it.id == "email" }?.value ?: ""
            val password = stateSnapshot.fields.firstOrNull { it.id == "pass" }?.value ?: ""

            loginManager.initializeUserSession(
                authInfo.userId,
                authInfo.token
            )

            accountManager.updateLastLoginSetting(
                AccountManager.UserSettings(email, password)
            )

            updateState { s ->
                s.withAuthDetails(
                    authDetails = s.authDetails.copy(
                        buttons = s.buttons.updateButton(REGISTER_BUTTON) { btn ->
                            btn.copy(loading = false, enabled = false)
                        }

                    )
                )
            }

            sendEvent(RegEvents.RefreshAll)


        }.onFailure { t ->
            val message = when (t) {
                is ValidationException -> t.message ?: failMessage
                else -> failMessage
            }

            updateState { s ->
                s.withAuthDetails(
                    authDetails = s.authDetails.copy(
                        buttons = s.buttons.updateButton(REGISTER_BUTTON) { btn ->
                            btn.copy(loading = false, enabled = t !is ValidationException)
                        }

                    )
                )
            }

            sendEvent(RegEvents.ShowSnackbar(message))
        }

    }


    private fun navigateToLoginByEmail() = viewModelScope.launch {
        sendEvent(RegEvents.GoToLoginByEmail)
    }

    override fun onBackClick() {
        launchInViewModelScope {
            sendEvent(RegEvents.GoBack)
        }
    }

    override fun clickButton(button: ColorfulButtonUi) {
        launchInViewModelScope {
            when (button.id) {
                REGISTER_BUTTON -> register()
                NAVIGATION_BUTTON -> navigateToLoginByEmail()
                else -> Unit
            }
        }
    }

    override fun clickHyperlink(url: String, title: String) {
        launchInViewModelScope {
            sendEvent(RegEvents.GoToWebView(url, title))
        }
    }


    sealed class RegEvents : Event {
        data class ShowSnackbar(val message: String) : RegEvents()
        data class GoToWebView(val url: String, val title: String) : RegEvents()

        data object GoBack : RegEvents()
        data object GoToProfile : RegEvents()
        data object GoToLoginByEmail : RegEvents()
        data object GoToLogin : RegEvents()
        data object RefreshAll : RegEvents()

    }

    @Immutable
    data class RegState(
        val uiState: RegUiState = RegUiState.Loading,
        override val authDetails: AuthDetailsUi = AuthDetailsUi.Empty,
    ) : AuthState<RegState>(authDetails) {
        override fun withAuthDetails(authDetails: AuthDetailsUi): RegState =
            copy(authDetails = authDetails)

    }

    @Stable
    sealed interface RegUiState {
        data object Error : RegUiState
        data object Loading : RegUiState
        data object Success : RegUiState
    }
}
