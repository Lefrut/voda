package com.m.vodovoz.feature.payment_method

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.R
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.common.resources.ResourcesProvider
import com.m.vodovoz.core.navigation.getQueryParams
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.design_system.model.widgets.FieldUi
import com.m.vodovoz.design_system.model.withItems
import com.m.vodovoz.domain.general.model.order.PaymentMethodItemModel
import com.m.vodovoz.domain.general.model.widgets.FieldModel
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.payment_method.model.PaymentMethodEvent
import com.m.vodovoz.feature.payment_method.model.PaymentMethodItemUi
import com.m.vodovoz.feature.payment_method.model.PaymentMethodState
import com.m.vodovoz.feature.payment_method.model.PaymentMethodUiState
import com.m.vodovoz.feature.payment_method.model.mapToUi
import com.m.vodovoz.feature.payment_method.model.updateFieldValueIf
import com.m.vodovoz.ui.mvi.MviViewModel
import com.m.vodovoz.util.extensions.singleResult
import com.m.vodovoz.util.toRoundIntOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
@Stable
class PaymentMethodViewModel @Inject constructor(
    val tabManager: TabManager,
    savedStateHandle: SavedStateHandle,
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val resourcesProvider: ResourcesProvider,
) : MviViewModel<PaymentMethodState, PaymentMethodEvent>(PaymentMethodState()) {

    private val addressId = savedStateHandle.get<Long>("addressId") ?: -1
    private val orderDate = savedStateHandle.get<Long>("date")?.let { days ->
        LocalDate.ofEpochDay(days)
    } ?: LocalDate.now()
    private val paymentMethodId: String? = savedStateHandle["paymentMethodId"]
    private val paymentChange: String = savedStateHandle["paymentChange"] ?: ""
    private val useBalance: Boolean? = savedStateHandle["balance"]
    private val useBonuses: Boolean? = savedStateHandle["bonuses"]
    private val bonusesValue: Int? = savedStateHandle["bonusesValue"]

    private val queryParams = savedStateHandle.getQueryParams()

    fun navigateBack() = viewModelScope.launch {
        sendEvent(PaymentMethodEvent.GoBack)
    }

    init {
        fetchPaymentMethodDetails()
    }

    fun fetchPaymentMethodDetails() = viewModelScope.launch {
        val paymentDetailsResult = vodovozServiceRepository.getPaymentMethodDetails(
            addressId = addressId, date = orderDate, queryParams = queryParams
        ).singleResult()

        paymentDetailsResult.onSuccess { paymentDetails ->

            val paymentSections = paymentDetails.items.map { section ->
                section.toUi { items ->
                    items.mapToUi().map { item ->
                        val field = item.field
                        if (field != null && field.id == FieldModel.CHANGE_ID && paymentChange.isNotBlank())
                            item.copy(
                                field = field.copy(
                                    value = resourcesProvider.getString(
                                        R.string.price_text, paymentChange
                                    )
                                )
                            )
                        else item
                    }
                }
            }

            updateState { s ->
                s.copy(
                    title = paymentDetails.title,
                    button = paymentDetails.button.toUi().copy(enabled = false),
                    paymentSections = paymentSections,
                    uiState = PaymentMethodUiState.Success
                )


            }

            val paymentMethodItemUi = paymentSections.flatMap { section ->
                section.items
            }.firstOrNull { item ->
                item.id == paymentMethodId
            }

            if (paymentMethodItemUi != null) {
                changePaymentMethodItem(paymentMethodItemUi).join()
            }


            if (useBalance == true || useBonuses == true) {
                updateState { s ->
                    s.copy(
                        paymentSections = s.paymentSections.map { section ->
                            section.withItems { paymentItem ->
                                when (paymentItem.id) {
                                    PaymentMethodItemModel.BONUSES_ID -> {
                                        val field = paymentItem.field
                                        paymentItem.copy(
                                            value = useBonuses == true,
                                            field = bonusesValue?.let {
                                                field?.copy(
                                                    value = bonusesValue.toString()
                                                )
                                            } ?: field
                                        )
                                    }

                                    PaymentMethodItemModel.BALANCE_ID -> {
                                        paymentItem.copy(
                                            value = useBalance == true
                                        )
                                    }

                                    else -> paymentItem
                                }
                            }
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
        val paymentSectionsItems = stateSnapshot.paymentSections.flatMap {
            it.items
        }

        val paymentBalance = paymentSectionsItems.firstOrNull { item ->
            item.id == PaymentMethodItemModel.BALANCE_ID
        }
        val paymentBonuses = paymentSectionsItems
            .firstOrNull { item ->
                item.id == PaymentMethodItemModel.BONUSES_ID
            }

        val paymentMethod = paymentSectionsItems
            .firstOrNull { item ->
                !item.isSwitch && item.value
            } ?: return@launch

        sendEvent(
            PaymentMethodEvent.GoBackToOrdering(
                paymentMethod = paymentMethod,
                paymentBalance = paymentBalance?.updateFieldValueIf(paymentBalance.value),
                paymentBonuses = paymentBonuses?.updateFieldValueIf(paymentBonuses.value)
            )
        )
    }

    fun changePaymentMethodItem(paymentMethod: PaymentMethodItemUi) = viewModelScope.launch {
        updateState { s ->
            val sections = s.paymentSections.map { section ->
                section.copy(
                    title = section.title,
                    items = section.items.map { item ->
                        when {
                            item.id == paymentMethod.id && item.isSwitch -> {
                                item.copy(value = !item.value)
                            }

                            item.id == paymentMethod.id && !item.isSwitch -> {
                                item.copy(value = true)
                            }

                            !paymentMethod.isSwitch && !item.isSwitch -> {
                                item.copy(value = false)
                            }

                            else -> item
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
                    section.withItems { sectionItem ->
                        if (sectionItem.id == item.id) {
                            val updatedFieldValue = updatedField.value
                            val updatedFieldIntValue =
                                updatedFieldValue.filter { it.isDigit() }.toRoundIntOrNull()
                                    .takeIf { price -> price != null && price > 0 }

                            sectionItem.copy(
                                field = field.copy(
                                    value = when (field.id) {
                                        FieldModel.CHANGE_ID -> updatedFieldIntValue?.let { price ->
                                            resourcesProvider.getString(
                                                R.string.price_text,
                                                price
                                            )
                                        }

                                        FieldModel.BONUS_ID -> updatedFieldIntValue?.let { intValue ->
                                            val maxFieldValue = sectionItem.maxFieldValue
                                            val finishIntValue =
                                                if (maxFieldValue != null && maxFieldValue < intValue) {
                                                    maxFieldValue
                                                } else intValue
                                            finishIntValue.toString()
                                        }

                                        else -> updatedFieldValue
                                    } ?: ""
                                )
                            )
                        } else sectionItem
                    }
                }
            )
        }
    }

}
