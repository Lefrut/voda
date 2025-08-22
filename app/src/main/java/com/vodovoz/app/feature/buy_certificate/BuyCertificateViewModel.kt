package com.vodovoz.app.feature.buy_certificate

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.R
import com.vodovoz.app.ui.mvi.Event
import com.vodovoz.app.ui.mvi.MviViewModel
import com.vodovoz.app.ui.mvi.State
import kotlinx.coroutines.flow.update
import com.vodovoz.app.common.resources.ResourcesProvider
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.PaymentTypeUi
import com.vodovoz.app.design_system.model.VodovozPlaceholderUi
import com.vodovoz.app.design_system.model.mapToUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.design_system.model.widgets.FieldUi
import com.vodovoz.app.design_system.model.widgets.checkFields
import com.vodovoz.app.design_system.model.widgets.getErrorText
import com.vodovoz.app.design_system.model.widgets.mapToDomain
import com.vodovoz.app.design_system.model.widgets.updateFieldAndResetError
import com.vodovoz.app.design_system.model.widgets.vodovozValidators
import com.vodovoz.app.domain.general.model.widgets.toQueries
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.buy_certificate.model.BuyCertificateCodesUi
import com.vodovoz.app.feature.buy_certificate.model.BuyCertificateErrorsUi
import com.vodovoz.app.feature.buy_certificate.model.BuyCertificateTabUi
import com.vodovoz.app.feature.buy_certificate.model.CertificateUi
import com.vodovoz.app.feature.buy_certificate.model.FAQUi
import com.vodovoz.app.feature.buy_certificate.model.CertificatePaymentInfoUi
import com.vodovoz.app.feature.buy_certificate.model.mapToUi
import com.vodovoz.app.feature.buy_certificate.model.toUi
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BuyCertificateViewModel @Inject constructor(
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val resourcesProvider: ResourcesProvider,
) : MviViewModel<BuyCertificateViewModel.BuyCertificateState, BuyCertificateViewModel.BuyCertificateEvents>(
    BuyCertificateState()
) {

    init {
        fetchBuyCertificateDetails()
    }

    fun fetchBuyCertificateDetails() = viewModelScope.launch {
        val buyCertificateDetailsResult =
            vodovozServiceRepository.getBuyCertificateDetails().singleResult()

        buyCertificateDetailsResult.onSuccess { buyCertificateDetails ->
            _state.update { s ->

                val tabs = buyCertificateDetails.tabs.mapToUi()
                val paymentTypes = buyCertificateDetails.paymentTypes.mapToUi()
                val certificates = buyCertificateDetails.certificates.mapToUi()

                s.copy(
                    uiState = if (s.uiState is BuyCertificateUiState.Success) s.uiState else BuyCertificateUiState.Body,
                    title = buyCertificateDetails.title,
                    certificates = certificates,
                    currentCertificate = certificates.lastOrNull() ?: s.currentCertificate,
                    certificatesTitle = buyCertificateDetails.certificatesTitle,
                    tabs = tabs,
                    button = buyCertificateDetails.button.toUi(),
                    paymentTitle = buyCertificateDetails.paymentTitle,
                    faq = buyCertificateDetails.faq?.toUi(),
                    paymentTypes = paymentTypes,
                    codes = buyCertificateDetails.codes.toUi(),
                    currentTab = tabs.firstOrNull() ?: s.currentTab,
                    currentPaymentType = paymentTypes.firstOrNull() ?: s.currentPaymentType
                )
            }
        }.onFailure {
            _state.update { s ->
                s.copy(uiState = BuyCertificateUiState.Error)
            }
        }
    }

    fun selectCertificate(certificate: CertificateUi) = viewModelScope.launch {
        _state.update { s ->
            s.copy(
                currentCertificate = certificate,
                errors = s.errors.copy(certificate = false)
            )
        }
    }

    fun openLink(url: String) = viewModelScope.launch {
        sendEvent(BuyCertificateEvents.OpenLink(url))
    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(BuyCertificateEvents.GoBack)
    }

    fun selectTab(tab: BuyCertificateTabUi) = viewModelScope.launch {
        _state.update { s ->
            s.copy(currentTab = tab)
        }
    }

    fun changeField(field: FieldUi, updatedField: FieldUi) = viewModelScope.launch {
        _state.update { s ->
            s.copy(
                currentTab = s.currentTab.copy(
                    fields = s.currentTab.fields.updateFieldAndResetError(field, updatedField)
                )
            )
        }
    }

    fun selectPaymentType(paymentType: PaymentTypeUi) = viewModelScope.launch {
        _state.update { s ->
            s.copy(
                currentPaymentType = paymentType,
                errors = s.errors.copy(payment = false)
            )
        }
    }

    fun activateButton(button: ColorfulButtonUi) = viewModelScope.launch {
        when (button.id) {
            "oformlenie" -> {
                buyCertificate()
            }

            "auth" -> {
                sendEvent(BuyCertificateEvents.GoToProfile)
            }
        }
    }

    private fun buyCertificate() = viewModelScope.launch {
        val currentTab = stateSnapshot.currentTab

        currentTab.fields.checkFields(
            putErrors = true,
            getSupportingText = { field ->
                field.getErrorText { id ->
                    resourcesProvider.getString(id)
                }
            },
            validators = vodovozValidators
        ) { fields, isValid ->
            val certificateError = stateSnapshot.currentCertificate == CertificateUi.Empty
            val paymentError = stateSnapshot.currentPaymentType == PaymentTypeUi.Empty

            _state.update { s ->
                s.copy(
                    errors = s.errors.copy(
                        certificate = certificateError,
                        payment = paymentError
                    ),
                    currentTab = currentTab.copy(
                        fields = fields
                    ),
                )
            }

            if (!isValid || certificateError || paymentError) return@launch
        }

        _state.update { s ->
            s.copy(button = s.button.copy(loading = true))
        }

        val certificate = stateSnapshot.currentCertificate
        val tab = stateSnapshot.currentTab
        val fields = tab.fields
        val codes = stateSnapshot.codes
        val paymentType = stateSnapshot.currentPaymentType


        val buyCertificateResult = vodovozServiceRepository.buyCertificate(
            mapOf(
                codes.certificates to certificate.id.toString(),
                codes.payment to paymentType.id.toString(),
                codes.tabs to tab.id.toString()
            ) + fields.mapToDomain().toQueries()
        ).singleResult()

        buyCertificateResult.onSuccess { buyCertificate ->
            val paymentInfo = buyCertificate.payment.toUi()
            _state.update { s ->
                s.copy(
                    uiState = BuyCertificateUiState.Success(buyCertificate.placeholder.toUi()),
                    paymentInfo = paymentInfo,
                    button = s.button.copy(loading = false)
                )
            }
        }.onFailure {
            sendEvent(BuyCertificateEvents.ShowToast(resourcesProvider.getString(R.string.order_failed)))

            _state.update { s ->
                s.copy(button = s.button.copy(loading = false))
            }
        }


    }

    fun pay() = viewModelScope.launch {
        val paymentInfo = stateSnapshot.paymentInfo ?: return@launch
        if (paymentInfo.browser) {
            sendEvent(BuyCertificateEvents.OpenUrl(paymentInfo.url))
        } else {
            sendEvent(BuyCertificateEvents.GoToWebView(paymentInfo.url))
        }

    }

    fun navigateToFAQ(faqUi: FAQUi) = viewModelScope.launch {
        sendEvent(BuyCertificateEvents.GoToFAQ(faqUi))
    }

    @Immutable
    data class BuyCertificateState(
        val title: String = "",
        val uiState: BuyCertificateUiState = BuyCertificateUiState.Loading,
        val codes: BuyCertificateCodesUi = BuyCertificateCodesUi.Empty,
        val certificatesTitle: String = "",
        val certificates: List<CertificateUi> = emptyList(),
        val currentCertificate: CertificateUi = CertificateUi.Empty,
        val tabs: List<BuyCertificateTabUi> = emptyList(),
        val currentTab: BuyCertificateTabUi = BuyCertificateTabUi.Empty,
        val button: ColorfulButtonUi = ColorfulButtonUi.Empty,
        val paymentTitle: String = "",
        val paymentTypes: List<PaymentTypeUi> = emptyList(),
        val currentPaymentType: PaymentTypeUi = PaymentTypeUi.Empty,
        val errors: BuyCertificateErrorsUi = BuyCertificateErrorsUi.Empty,
        val faq: FAQUi? = null,
        val paymentInfo: CertificatePaymentInfoUi? = null,
    ) : State

    sealed interface BuyCertificateEvents : Event {

        data object GoBack : BuyCertificateEvents
        data object GoToProfile : BuyCertificateEvents

        data class OpenLink(val url: String) : BuyCertificateEvents
        data class GoToFAQ(val faq: FAQUi) : BuyCertificateEvents
        data class GoToWebView(val url: String) : BuyCertificateEvents
        data class OpenUrl(val url: String) : BuyCertificateEvents
        data class ShowToast(val message: String) : BuyCertificateEvents

    }


    @Stable
    sealed interface BuyCertificateUiState {
        data object Loading : BuyCertificateUiState
        data object Error : BuyCertificateUiState
        data object Body : BuyCertificateUiState
        data class Success(val placeholder: VodovozPlaceholderUi) : BuyCertificateUiState
    }
}