package com.vodovoz.app.feature.auth.reg

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.R
import com.vodovoz.app.common.account.AccountManager
import com.vodovoz.app.common.account.LoginManager
import com.vodovoz.app.common.agreement.AgreementController
import com.vodovoz.app.common.content.Event
import com.vodovoz.app.common.content.PagingContractViewModel
import com.vodovoz.app.common.content.updateData
import com.vodovoz.app.common.resources.ResourcesProvider
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.mapToUi
import com.vodovoz.app.design_system.model.updateButton
import com.vodovoz.app.design_system.model.widgets.CheckboxUi
import com.vodovoz.app.design_system.model.widgets.EmptyTextValidator
import com.vodovoz.app.design_system.model.widgets.FieldUi
import com.vodovoz.app.design_system.model.widgets.PhoneNumberValidator
import com.vodovoz.app.design_system.model.widgets.checkFields
import com.vodovoz.app.design_system.model.widgets.getErrorText
import com.vodovoz.app.design_system.model.widgets.updateCheckbox
import com.vodovoz.app.design_system.model.widgets.updateFieldAndResetError
import com.vodovoz.app.domain.general.model.ValidationException
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.auth.model.AuthDetailsUi
import com.vodovoz.app.feature.auth.model.AuthState
import com.vodovoz.app.feature.auth.model.agreementIsCheckedWhenAvailable
import com.vodovoz.app.feature.auth.model.authValidators
import com.vodovoz.app.feature.auth.model.toUi
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class RegFlowViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val resourceProvider: ResourcesProvider,
    private val loginManager: LoginManager,
) : PagingContractViewModel<RegFlowViewModel.RegState, RegFlowViewModel.RegEvents>(RegState()) {

    companion object {
        const val REGISTER_BUTTON = "otpravka"
        const val NAVIGATION_BUTTON = "auth"
    }

    init {
        fetchRegisterDetails()
    }

    private fun fetchRegisterDetails() = viewModelScope.launch {
        uiStateListener.updateData { s -> s.copy(uiState = RegUiState.Loading) }

        val registerFieldsResult =
            vodovozServiceRepository.getRegisterDetails().singleResult()

        registerFieldsResult.onSuccess { registerDetails ->
            uiStateListener.updateData { s ->
                s.copy(
                    uiState = RegUiState.Success,
                    authDetails = registerDetails.toUi(AgreementController.getText()).copy(
                        buttons = registerDetails.buttons.mapToUi()
                            .updateButton(REGISTER_BUTTON) { btn ->
                                btn.copy(enabled = false)
                            }

                    )
                )
            }
        }.onFailure {
            uiStateListener.updateData { s -> s.copy(uiState = RegUiState.Error) }
        }
    }

    private fun register() = viewModelScope.launch {
        val isValid = stateSnapshot.fields.checkFields(
            putErrors = true,
            getSupportingText = { field -> field.getErrorText { id -> resourceProvider.getString(id) } }
        ) { updatedFields, _ ->
            uiStateListener.updateData { s ->
                s.copy(
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



        uiStateListener.updateData { s ->
            s.copy(
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

            uiStateListener.updateData { s ->
                s.copy(
                    authDetails = s.authDetails.copy(
                        buttons = s.buttons.updateButton(REGISTER_BUTTON) { btn ->
                            btn.copy(loading = false, enabled = false)
                        }

                    )
                )
            }

            eventListener.emit(RegEvents.RefreshAll)


        }.onFailure { t ->
            val message = when (t) {
                is ValidationException -> t.message ?: failMessage
                else -> failMessage
            }

            uiStateListener.updateData { s ->
                s.copy(
                    authDetails = s.authDetails.copy(
                        buttons = s.buttons.updateButton(REGISTER_BUTTON) { btn ->
                            btn.copy(loading = false, enabled = t !is ValidationException)
                        }

                    )
                )
            }

            eventListener.emit(RegEvents.ShowSnackbar(message))
        }

    }


    fun navigateBack() = viewModelScope.launch {
        eventListener.emit(RegEvents.GoBack)
    }


    fun changeField(field: FieldUi, updatedField: FieldUi) = viewModelScope.launch {
        val updatedFields = stateSnapshot.fields.updateFieldAndResetError(field, updatedField)


        updatedFields.checkFields(
            validators = listOf(PhoneNumberValidator, EmptyTextValidator)
        ) { fields, isValid ->
            uiStateListener.updateData { s ->
                s.copy(
                    authDetails = s.authDetails.copy(
                        fields = fields,
                        buttons = s.buttons.updateButton(REGISTER_BUTTON) { btn ->
                            btn.copy(enabled = isValid && s.checkboxes.agreementIsCheckedWhenAvailable())
                        }
                    )
                )
            }
        }
    }

    fun openAgreementUrl(url: String, urlIndex: Int) = viewModelScope.launch {
        val title = AgreementController.getTitle(urlIndex) ?: ""
        eventListener.emit(RegEvents.GoToWebView(url, title))
    }

    private fun navigateToLoginByEmail() = viewModelScope.launch {
        eventListener.emit(RegEvents.GoToLoginByEmail)
    }

    fun activateButton(button: ColorfulButtonUi) = viewModelScope.launch {
        when (button.id) {
            REGISTER_BUTTON -> {
                register()
            }

            NAVIGATION_BUTTON -> {
                navigateToLoginByEmail()
            }

            else -> {

            }
        }
    }

    fun changeCheckbox(checkbox: CheckboxUi, updatedCheckbox: CheckboxUi) {
        uiStateListener.updateData { s ->
            val authDetails = s.authDetails
            val updatedCheckboxes = authDetails.checkboxes.updateCheckbox(
                checkbox, updatedCheckbox
            )

            s.copy(
                authDetails = authDetails.copy(
                    checkboxes = updatedCheckboxes,
                    buttons = authDetails.buttons.updateButton(REGISTER_BUTTON) { button ->
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
    ) : AuthState(authDetails)

    @Stable
    sealed interface RegUiState {
        data object Error : RegUiState
        data object Loading : RegUiState
        data object Success : RegUiState
    }
}