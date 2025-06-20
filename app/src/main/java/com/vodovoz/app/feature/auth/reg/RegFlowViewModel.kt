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
import com.vodovoz.app.common.content.State
import com.vodovoz.app.common.content.updateData
import com.vodovoz.app.common.resources.ResourcesProvider
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.mapToUi
import com.vodovoz.app.design_system.model.updateButton
import com.vodovoz.app.design_system.model.widgets.EmptyTextValidator
import com.vodovoz.app.design_system.model.widgets.FieldUi
import com.vodovoz.app.design_system.model.widgets.PhoneNumberValidator
import com.vodovoz.app.design_system.model.widgets.checkFields
import com.vodovoz.app.design_system.model.widgets.getErrorText
import com.vodovoz.app.design_system.model.widgets.mapToDomain
import com.vodovoz.app.design_system.model.widgets.mapToUi
import com.vodovoz.app.design_system.model.widgets.updateFieldAndResetError
import com.vodovoz.app.domain.general.model.ValidationException
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.sitestate.SiteStateManager
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
    private val siteStateManager: SiteStateManager,
    private val loginManager: LoginManager
) : PagingContractViewModel<RegFlowViewModel.RegState, RegFlowViewModel.RegEvents>(RegState()) {

    companion object {
        const val REGISTER_BUTTON = "otpravka"
        const val NAVIGATION_BUTTON = "auth"
    }

    init {
        viewModelScope.launch { siteStateManager.requestSiteState() }
        fetchRegisterDetails()
    }

    fun fetchRegisterDetails() = viewModelScope.launch {
        uiStateListener.updateData { s -> s.copy(uiState = UiState.Loading) }

        val registerFieldsResult =
            vodovozServiceRepository.getRegisterDetails().singleResult()

        registerFieldsResult.onSuccess { registerDetails ->
            uiStateListener.updateData { s ->
                s.copy(
                    uiState = UiState.Success,
                    fields = registerDetails.fields.mapToUi(),
                    title = registerDetails.title,
                    showAgreement = registerDetails.haveAgreement,
                    agreementTextHtml = AgreementController.getText(),
                    buttons = registerDetails.buttons.mapToUi()
                        .updateButton(REGISTER_BUTTON) { btn ->
                            btn.copy(
                                enabled = false
                            )
                        }
                )
            }
        }.onFailure {
            uiStateListener.updateData { s -> s.copy(uiState = UiState.Error) }
        }
    }

    fun register() = viewModelScope.launch {
        val isValid = dataState.fields.checkFields(
            putErrors = true,
            getSupportingText = { field -> field.getErrorText { id -> resourceProvider.getString(id) } }
        ) { updatedFields, _ ->
            uiStateListener.updateData { s ->
                s.copy(
                    fields = updatedFields,
                    buttons = s.buttons.updateButton(REGISTER_BUTTON) { btn ->
                        btn.copy(enabled = false)
                    }
                )
            }
        }

        if (!isValid) return@launch



        uiStateListener.updateData { s ->
            s.copy(
                buttons = s.buttons.updateButton(REGISTER_BUTTON) { btn ->
                    btn.copy(loading = true)
                }
            )
        }

        val failMessage = resourceProvider.getString(R.string.error_registration)

        val registerResult = vodovozServiceRepository.register(
            dataState.fields.mapToDomain()
        ).singleResult()

        registerResult.onSuccess { authInfo ->

            val email = dataState.fields.firstOrNull { it.id == "email" }?.value ?: ""
            val password = dataState.fields.firstOrNull { it.id == "pass" }?.value ?: ""

            loginManager.initializeUserSession(
                authInfo.userId,
                authInfo.token
            )

            accountManager.updateLastLoginSetting(
                AccountManager.UserSettings(email, password)
            )

            uiStateListener.updateData { s ->
                s.copy(
                    buttons = s.buttons.updateButton(REGISTER_BUTTON) { btn ->
                        btn.copy(loading = false, enabled = false)
                    }
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
                    buttons = s.buttons.updateButton(REGISTER_BUTTON) { btn ->
                        btn.copy(loading = false, enabled = t !is ValidationException)
                    }
                )
            }

            eventListener.emit(RegEvents.ShowSnackbar(message))
        }

    }


    fun navigateBack() = viewModelScope.launch {
        eventListener.emit(RegEvents.GoBack)
    }


    fun changeField(field: FieldUi, updatedField: FieldUi) = viewModelScope.launch {
        val updatedFields = dataState.fields.updateFieldAndResetError(field, updatedField)


        updatedFields.checkFields(
            validators = listOf(PhoneNumberValidator, EmptyTextValidator)
        ) { fields, isValid ->
            uiStateListener.updateData { s ->
                s.copy(
                    fields = fields,
                    buttons = s.buttons.updateButton(REGISTER_BUTTON) { btn ->
                        btn.copy(enabled = isValid && (s.agreementChecked || !s.showAgreement))
                    }
                )
            }
        }
    }

    fun openAgreementUrl(url: String, urlIndex: Int) = viewModelScope.launch {
        val title = AgreementController.getTitle(urlIndex) ?: ""
        eventListener.emit(RegEvents.GoToWebView(url, title))
    }

    fun checkAgreement(checked: Boolean) = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(
                agreementChecked = checked,
                buttons = s.buttons.updateButton(REGISTER_BUTTON) { btn ->
                    btn.copy(enabled = s.fields.checkFields() && (s.agreementChecked || !s.showAgreement))
                }
            )
        }
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
        val agreementTextHtml: String = "",
        val showAgreement: Boolean = false,
        val agreementChecked: Boolean = true,
        val fields: List<FieldUi> = emptyList(),
        val uiState: UiState = UiState.Loading,
        val title: String = "",
        val buttons: List<ColorfulButtonUi> = emptyList(),
    ) : State

    sealed interface UiState {
        data object Error : UiState
        data object Loading : UiState
        data object Success : UiState
    }
}