package com.vodovoz.app.feature.auth.recover_password

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.R
import com.vodovoz.app.common.agreement.AgreementController
import com.vodovoz.app.common.resources.ResourcesProvider
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.design_system.model.updateButton
import com.vodovoz.app.design_system.model.widgets.FieldUi
import com.vodovoz.app.design_system.model.widgets.checkFields
import com.vodovoz.app.design_system.model.widgets.mapToDomain
import com.vodovoz.app.design_system.model.widgets.mapToUi
import com.vodovoz.app.design_system.model.widgets.updateField
import com.vodovoz.app.design_system.model.widgets.updateFieldAndResetError
import com.vodovoz.app.domain.general.model.RequestException
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.auth.recover_password.model.RecoverPasswordEvent
import com.vodovoz.app.feature.auth.recover_password.model.RecoverPasswordState
import com.vodovoz.app.feature.auth.recover_password.model.RecoverPasswordUiState
import com.vodovoz.app.feature.sitestate.SiteStateManager
import com.vodovoz.app.ui.mvi.MviViewModel
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
@HiltViewModel
class RecoverPasswordViewModel @Inject constructor(
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val resourcesProvider: ResourcesProvider,
    private val siteStateManager: SiteStateManager,
) : MviViewModel<RecoverPasswordState, RecoverPasswordEvent>(
    RecoverPasswordState()
) {

    init {
        fetchRecoverPasswordDetails()
    }

    companion object {
        private const val RECOVER_PASSWORD_BUTTON = "otpravka"
    }

    fun navigateBack() = viewModelScope.launch {
        _events.emit(RecoverPasswordEvent.GoBack)
    }

    fun activateButton(button: ColorfulButtonUi) = viewModelScope.launch {
        when (button.id) {
            RECOVER_PASSWORD_BUTTON -> {
                recoverPassword()
            }

            else -> {

            }
        }
    }

    fun fetchRecoverPasswordDetails() = viewModelScope.launch {
        _state.update { s ->
            s.copy(uiState = RecoverPasswordUiState.Loading)
        }

        val recoverPasswordDeferred =
            async { vodovozServiceRepository.getRecoverPasswordDetails().singleResult() }

        siteStateManager.requestSiteState()

        val recoverPasswordDetailsResult = recoverPasswordDeferred.await()
        val agreementText = AgreementController.getText()

        recoverPasswordDetailsResult.onSuccess { recoverPasswordDetails ->

            val buttons = recoverPasswordDetails.buttons.map { colorfulButtonModel ->
                colorfulButtonModel.toUi()
            }.updateButton(RECOVER_PASSWORD_BUTTON) { btn ->
                btn.copy(enabled = false)
            }

            _state.update { s ->
                s.copy(
                    description = recoverPasswordDetails.description,
                    title = recoverPasswordDetails.title,
                    buttons = buttons,
                    fields = recoverPasswordDetails.fields.mapToUi(),
                    agreementHtml = agreementText,
                    uiState = RecoverPasswordUiState.Body,
                    showAgreement = recoverPasswordDetails.haveAgreement,
                )
            }
        }.onFailure {
            _state.update { s ->
                s.copy(uiState = RecoverPasswordUiState.Error)
            }
        }
    }

    private fun recoverPassword() = viewModelScope.launch {

        _state.update { s ->
            s.copy(
                buttons = s.buttons.updateButton(RECOVER_PASSWORD_BUTTON) { it.copy(loading = true) }
            )
        }


        val requestPhoneCodeResult = vodovozServiceRepository.recoverPassword(
            stateSnapshot.fields.mapToDomain()
        ).singleResult()

        requestPhoneCodeResult.onFailure { t ->
            val errorMessage = when (t) {
                is RequestException -> {
                    ""
                }

                else -> {
                    ""
                }
            }


            _state.update { s ->
                val field = s.fields.lastOrNull() ?: return@update s

                s.copy(
                    fields = s.fields.updateField(
                        field,
                        field.copy(
                            isError = true,
                            supportingText = errorMessage
                        )
                    ),
                    buttons = s.buttons.updateButton(RECOVER_PASSWORD_BUTTON) { it.copy(loading = false) }
                )
            }


        }.onSuccess { placeholder ->
            _state.update { s ->
                s.copy(
                    uiState = RecoverPasswordUiState.Success(placeholder.toUi()),
                    buttons = s.buttons.updateButton(RECOVER_PASSWORD_BUTTON) { it.copy(loading = false) }
                )
            }
        }

    }

    fun changeField(field: FieldUi, updatedField: FieldUi) = viewModelScope.launch {
        _state.update { s ->
            val updatedFields = s.fields.updateFieldAndResetError(field, updatedField)

            s.copy(
                fields = updatedFields,
                buttons = s.buttons.updateButton(RECOVER_PASSWORD_BUTTON) { button ->
                    button.copy(enabled = updatedFields.checkFields() && (s.agreementChecked || !s.showAgreement))
                },
            )
        }
    }

    fun checkAgreement(newValue: Boolean) = viewModelScope.launch {
        _state.update { s ->
            s.copy(
                agreementChecked = newValue,
                buttons = s.buttons.updateButton(RECOVER_PASSWORD_BUTTON) { button ->
                    button.copy(enabled = s.fields.checkFields() && newValue)
                }
            )
        }

    }

    fun navigateToWebView(url: String, urlIndex: Int) = viewModelScope.launch {
        val title =
            AgreementController.getTitle(urlIndex) ?: resourcesProvider.getString(R.string.space)
        _events.emit(RecoverPasswordEvent.GoToWebView(url, title))
    }

}