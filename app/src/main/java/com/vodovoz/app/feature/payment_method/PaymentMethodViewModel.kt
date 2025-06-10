package com.vodovoz.app.feature.payment_method

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.payment_method.model.PaymentMethodEvent
import com.vodovoz.app.feature.payment_method.model.PaymentMethodItemUi
import com.vodovoz.app.feature.payment_method.model.PaymentMethodState
import com.vodovoz.app.feature.payment_method.model.PaymentMethodUiState
import com.vodovoz.app.feature.payment_method.model.mapToUi
import com.vodovoz.app.ui.mvi.MviViewModel
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
@Stable
class PaymentMethodViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vodovozServiceRepository: VodovozServiceRepository,
) : MviViewModel<PaymentMethodState, PaymentMethodEvent>(PaymentMethodState()) {

    private val addressId = savedStateHandle.get<Int>("addressId") ?: navigateBack().let { -1 }
    private val orderDate = savedStateHandle.get<Long>("date")?.let { days ->
        LocalDate.ofEpochDay(days)
    } ?: navigateBack().let { LocalDate.now() }

    fun navigateBack() = viewModelScope.launch {
        _events.emit(PaymentMethodEvent.GoBack)
    }

    init {
        fetchPaymentMethodDetails()
    }

    fun fetchPaymentMethodDetails() = viewModelScope.launch {
        val paymentDetailsResult = vodovozServiceRepository.getPaymentMethodDetails(
            addressId, orderDate
        ).singleResult()

        paymentDetailsResult.onSuccess { paymentDetails ->
            _state.update { s ->
                s.copy(
                    title = paymentDetails.title,
                    button = paymentDetails.button.toUi(),
                    paymentSections = paymentDetails.items.map { section ->
                        section.toUi { items -> items.mapToUi() }
                    },
                    uiState = PaymentMethodUiState.Success
                )
            }

        }.onFailure {
            _state.update { s ->
                s.copy(uiState = PaymentMethodUiState.Error)
            }
        }
    }

    fun choosePaymentMethod() {
        //todo - need realization
    }

    fun changePaymentMethodItem(paymentMethod: PaymentMethodItemUi) = viewModelScope.launch {
        _state.update { s ->
            val sections = s.paymentSections.map { section ->
                section.copy(
                    title = section.title,
                    items = section.items.map {
                        when {
                            it.id == paymentMethod.id && it.isSwitch -> it.copy(value = !it.value)
                            it.id == paymentMethod.id && !it.isSwitch -> it.copy(value = true)
                            !paymentMethod.isSwitch && !it.isSwitch -> it.copy(value = false)
                            else -> it
                        }
                    }

                )
            }

            s.copy(
                paymentSections = sections
            )
        }
    }

}