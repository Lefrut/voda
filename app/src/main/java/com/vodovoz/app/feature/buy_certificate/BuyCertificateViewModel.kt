package com.vodovoz.app.feature.buy_certificate

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.R
import com.vodovoz.app.common.content.Event
import com.vodovoz.app.common.content.PagingContractViewModel
import com.vodovoz.app.common.content.State
import com.vodovoz.app.common.content.updateData
import com.vodovoz.app.common.resources.ResourcesProvider
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.PaymentTypeUi
import com.vodovoz.app.design_system.model.VodovozPlaceholderUi
import com.vodovoz.app.design_system.model.mapToUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.domain.general.model.toQueries
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.buy_certificate.model.BuyCertificateCodesUi
import com.vodovoz.app.feature.buy_certificate.model.BuyCertificateErrorsUi
import com.vodovoz.app.feature.buy_certificate.model.BuyCertificateTabUi
import com.vodovoz.app.feature.buy_certificate.model.CertificateUi
import com.vodovoz.app.feature.buy_certificate.model.FAQUi
import com.vodovoz.app.feature.buy_certificate.model.PaymentInfoUi
import com.vodovoz.app.feature.buy_certificate.model.mapToUi
import com.vodovoz.app.feature.buy_certificate.model.toUi
import com.vodovoz.app.design_system.model.widgets.FieldUi
import com.vodovoz.app.design_system.model.widgets.checkFields
import com.vodovoz.app.design_system.model.widgets.getErrorText
import com.vodovoz.app.design_system.model.widgets.mapToDomain
import com.vodovoz.app.design_system.model.widgets.updateFieldAndResetError
import com.vodovoz.app.design_system.model.widgets.vodovozValidators
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BuyCertificateViewModel @Inject constructor(
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val resourcesProvider: ResourcesProvider,
) : PagingContractViewModel<BuyCertificateViewModel.BuyCertificateState, BuyCertificateViewModel.BuyCertificateEvents>(
    BuyCertificateState()
) {


    fun fetchBuyCertificateDetails() = viewModelScope.launch {
        if (dataState.uiState is BuyCertificateUiState.Success) return@launch

        uiStateListener.updateData { s ->
            s.copy(uiState = BuyCertificateUiState.Loading)
        }

        val buyCertificateDetailsResult =
            vodovozServiceRepository.getBuyCertificateDetails().singleResult()

        buyCertificateDetailsResult.onSuccess { buyCertificateDetails ->
            uiStateListener.updateData { s ->

                val tabs = buyCertificateDetails.tabs.mapToUi()
                val paymentTypes = buyCertificateDetails.paymentTypes.mapToUi()

                s.copy(
                    uiState = BuyCertificateUiState.Body,
                    title = buyCertificateDetails.title,
                    certificates = buyCertificateDetails.certificates.mapToUi(),
                    certificatesTitle = buyCertificateDetails.certificatesTitle,
                    tabs = tabs,
                    button = buyCertificateDetails.button.toUi(),
                    paymentTitle = buyCertificateDetails.paymentTitle,
                    faq = buyCertificateDetails.faq.toUi(),
                    paymentTypes = paymentTypes,
                    codes = buyCertificateDetails.codes.toUi(),
                    currentTab = tabs.firstOrNull() ?: s.currentTab,
                    currentPaymentType = paymentTypes.firstOrNull() ?: s.currentPaymentType
                )
            }
        }.onFailure {
            uiStateListener.updateData { s ->
                s.copy(uiState = BuyCertificateUiState.Error)
            }
        }
    }

    fun selectCertificate(certificate: CertificateUi) = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(
                currentCertificate = certificate,
                errors = s.errors.copy(certificate = false)
            )
        }
    }

    fun openLink(url: String) = viewModelScope.launch {
        eventListener.emit(BuyCertificateEvents.OpenLink(url))
    }

    fun navigateBack() = viewModelScope.launch {
        eventListener.emit(BuyCertificateEvents.GoBack)
    }

    fun selectTab(tab: BuyCertificateTabUi) = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(currentTab = tab)
        }
    }

    fun changeField(field: FieldUi, updatedField: FieldUi) = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(
                currentTab = s.currentTab.copy(
                    fields = s.currentTab.fields.updateFieldAndResetError(field, updatedField)
                )
            )
        }
    }

    fun selectPaymentType(paymentType: PaymentTypeUi) = viewModelScope.launch {
        uiStateListener.updateData { s ->
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
                eventListener.emit(BuyCertificateEvents.GoToProfile)
            }
        }
    }

    private fun buyCertificate() = viewModelScope.launch {
        val currentTab = dataState.currentTab

        currentTab.fields.checkFields(
            putErrors = true,
            getSupportingText = { field ->
                field.getErrorText { id ->
                    resourcesProvider.getString(id)
                }
            },
            validators = vodovozValidators
        ) { fields, isValid ->
            val certificateError = dataState.currentCertificate == CertificateUi.Empty
            val paymentError = dataState.currentPaymentType == PaymentTypeUi.Empty

            uiStateListener.updateData { s ->
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

        uiStateListener.updateData { s ->
            s.copy(button = s.button.copy(loading = true))
        }

        val certificate = dataState.currentCertificate
        val tab = dataState.currentTab
        val fields = tab.fields
        val codes = dataState.codes
        val paymentType = dataState.currentPaymentType


        val buyCertificateResult = vodovozServiceRepository.buyCertificate(
            mapOf(
                codes.certificates to certificate.id.toString(),
                codes.payment to paymentType.id.toString(),
                codes.tabs to tab.id.toString()
            ) + fields.mapToDomain().toQueries()
        ).singleResult()

        buyCertificateResult.onSuccess { buyCertificate ->
            val paymentInfo = buyCertificate.payment.toUi()
            uiStateListener.updateData { s ->
                s.copy(
                    uiState = BuyCertificateUiState.Success(buyCertificate.placeholder.toUi()),
                    paymentInfo = paymentInfo,
                    button = s.button.copy(loading = false)
                )
            }
        }.onFailure {
            eventListener.emit(BuyCertificateEvents.ShowToast(resourcesProvider.getString(R.string.order_failed)))

            uiStateListener.updateData { s ->
                s.copy(button = s.button.copy(loading = false))
            }
        }


    }

    fun pay() = viewModelScope.launch {
        val paymentInfo = dataState.paymentInfo ?: return@launch
        if (paymentInfo.browser) {
            eventListener.emit(BuyCertificateEvents.OpenUrl(paymentInfo.url))
        } else {
            eventListener.emit(BuyCertificateEvents.GoToWebView(paymentInfo.url))
        }

    }

    fun navigateToFAQ(faqUi: FAQUi) = viewModelScope.launch {
        eventListener.emit(BuyCertificateEvents.GoToFAQ(faqUi))
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
        val faq: FAQUi = FAQUi.Empty,
        val paymentInfo: PaymentInfoUi? = null,
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


    @Immutable
    sealed interface BuyCertificateUiState {
        data object Loading : BuyCertificateUiState
        data object Error : BuyCertificateUiState
        data object Body : BuyCertificateUiState
        data class Success(val placeholder: VodovozPlaceholderUi) : BuyCertificateUiState
    }
}