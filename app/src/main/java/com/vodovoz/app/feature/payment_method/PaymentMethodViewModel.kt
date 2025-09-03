package com.vodovoz.app.feature.payment_method

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.R
import com.vodovoz.app.common.resources.ResourcesProvider
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.design_system.model.widgets.FieldUi
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
    private val resourcesProvider: ResourcesProvider,
) : MviViewModel<PaymentMethodState, PaymentMethodEvent>(PaymentMethodState()) {

    private val addressId = savedStateHandle.get<Long>("addressId") ?: navigateBack().let { -1 }
    private val orderDate = savedStateHandle.get<Long>("date")?.let { days ->
        LocalDate.ofEpochDay(days)
    } ?: navigateBack().let { LocalDate.now() }
    private val paymentMethodId = savedStateHandle.get<String>("paymentMethodId")
    private val balance = savedStateHandle.get<Boolean>("balance")

    fun navigateBack() = viewModelScope.launch {
        sendEvent(PaymentMethodEvent.GoBack)
    }

    init {
        fetchPaymentMethodDetails()
    }

    fun fetchPaymentMethodDetails() = viewModelScope.launch {
        val paymentDetailsResult = vodovozServiceRepository.getPaymentMethodDetails(
            addressId, orderDate
        ).singleResult()

        paymentDetailsResult.onSuccess { paymentDetails ->

            val paymentSections = paymentDetails.items.map { section ->
                section.toUi { items -> items.mapToUi() }
            }

            updateState { s ->
                s.copy(
                    title = paymentDetails.title,
                    button = paymentDetails.button.toUi().copy(enabled = false),
                    paymentSections = paymentSections,
                    uiState = PaymentMethodUiState.Success
                )


            }

            val paymentMethodItemUi = paymentSections.flatMap {
                it.items
            }.firstOrNull { it.id == paymentMethodId }

            if (paymentMethodItemUi != null) {
                changePaymentMethodItem(paymentMethodItemUi).join()
            }
            if (balance != null) {
                updateState { s ->
                    s.copy(
                        paymentSections = s.paymentSections.map { section ->
                            section.copy(
                                title = section.title,
                                items = section.items.map { paymentItem ->
                                    if (paymentItem.id == "schet") {
                                        paymentItem.copy(value = balance)
                                    } else {
                                        paymentItem
                                    }
                                }
                            )
                        }

                    )
                }
            }


        }.onFailure {
            updateState { s ->
                s.copy(uiState = PaymentMethodUiState.Error)
            }
        }
    }

    fun choosePaymentMethod() = viewModelScope.launch {
        val paymentSections = stateSnapshot.paymentSections
        val paymentBalance = paymentSections
            .flatMap { it.items }
            .firstOrNull { it -> it.isSwitch }

        val paymentMethod = paymentSections
            .flatMap { it.items }
            .firstOrNull { !it.isSwitch && it.value } ?: return@launch
        sendEvent(PaymentMethodEvent.GoBackToOrdering(paymentMethod, paymentBalance))
    }

    fun changePaymentMethodItem(paymentMethod: PaymentMethodItemUi) = viewModelScope.launch {
        updateState { s ->
            val sections = s.paymentSections.map { section ->
                section.copy(
                    title = section.title,
                    items = section.items.map { it ->
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
                paymentSections = sections,
                button = s.button.copy(
                    enabled = sections.flatMap { it.items }.any { !it.isSwitch && it.value }
                )
            )
        }
    }

    fun changeField(item: PaymentMethodItemUi, field: FieldUi, updatedField: FieldUi) {
        updateState { s ->
            s.copy(
                paymentSections = s.paymentSections.map { section ->
                    section.copy(
                        items = section.items.map { sectionItem ->
                            if (sectionItem.id == item.id) {
                                sectionItem.copy(
                                    field = when (field.id) {
                                        "oplata" -> {
                                            val number = updatedField.value.filter { c ->
                                                c.isDigit()
                                            }.toIntOrNull()
                                            val value = number?.takeIf { it > 0 }?.let { price ->
                                                resourcesProvider.getString(
                                                    R.string.price_text,
                                                    price
                                                )
                                            } ?: ""

                                            updatedField.copy(value = value)
                                        }

                                        else -> {
                                            updatedField
                                        }
                                    }
                                )
                            } else sectionItem
                        }
                    )
                }
            )
        }
    }

}