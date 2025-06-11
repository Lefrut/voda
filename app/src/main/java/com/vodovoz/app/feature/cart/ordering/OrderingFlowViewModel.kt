package com.vodovoz.app.feature.cart.ordering

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.BuildConfig
import com.vodovoz.app.common.account.AccountManager
import com.vodovoz.app.common.cart.CartManager
import com.vodovoz.app.common.content.ErrorState
import com.vodovoz.app.common.content.Event
import com.vodovoz.app.common.content.PagingContractViewModel
import com.vodovoz.app.common.content.State
import com.vodovoz.app.common.content.toErrorState
import com.vodovoz.app.common.content.updateData
import com.vodovoz.app.data.MainRepository
import com.vodovoz.app.data.model.common.ResponseEntity
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.SectionUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.design_system.model.widgets.FieldUi
import com.vodovoz.app.design_system.model.widgets.toUi
import com.vodovoz.app.design_system.model.order.OrderSummaryItemUi
import com.vodovoz.app.design_system.model.order.mapToUi
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.cart.ordering.model.OrderNotifyItemUi
import com.vodovoz.app.feature.cart.ordering.model.OrderPaymentItemUi
import com.vodovoz.app.feature.cart.ordering.model.OrderRecipientItemUi
import com.vodovoz.app.feature.cart.ordering.model.mapToUi
import com.vodovoz.app.ui.model.AddressUI
import com.vodovoz.app.ui.model.custom.OrderingCompletedInfoBundleUI
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
@Stable
class OrderingFlowViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vodovozServiceRepository: VodovozServiceRepository,
) : PagingContractViewModel<OrderingFlowViewModel.OrderingState, OrderingFlowViewModel.OrderingEvents>(
    OrderingState(
    )
) {
    private val coupon = savedStateHandle.get<String>("coupon") ?: ""

    init {
        fetchOrderingDetails()
    }

    fun fetchOrderingDetails() = viewModelScope.launch {
        val orderingDetailsResult = vodovozServiceRepository.getOrderingDetails().singleResult()

        orderingDetailsResult.onSuccess { orderingDetails ->
            uiStateListener.updateData { s ->

                val notifySection = orderingDetails.notifySection.toUi { items ->
                    items.mapToUi()
                }

                s.copy(
                    title = orderingDetails.title,
                    comment = orderingDetails.commentField?.toUi(),
                    paymentSection = orderingDetails.paymentSection.toUi { items ->
                        items.mapToUi()
                    },
                    recipientSection = orderingDetails.recipientSection.toUi { items ->
                        items.mapToUi()
                    },
                    notifySection = notifySection,
                    totals = orderingDetails.totals.mapToUi(),
                    button = orderingDetails.button.toUi(),
                    selectedNotifyItem = s.selectedNotifyItem.takeIf {
                        it != OrderNotifyItemUi.Empty
                    } ?: notifySection.items.firstOrNull() ?: s.selectedNotifyItem,
                    uiState = OrderingUiState.Success
                )
            }
        }.onFailure {
            uiStateListener.updateData { s ->
                s.copy(
                    uiState = OrderingUiState.Error
                )
            }
        }
    }

//    fun regOrder(
//        comment: String = "",
//        name: String = "",
//        phone: String = "",
//        email: String = "",
//        companyName: String = "",
//        inn: String = "",
//        inputCash: String = "",
//        phoneForDriver: String = "",
//    ) {
//        viewModelScope.launch {
//
//            val userId = accountManager.fetchAccountId() ?: return@launch
//            val deviceInfo = application.getDeviceInfo()
//            val addressId = state.data.selectedAddressUI?.id
//            if (addressId == null) {
//                eventListener.emit(OrderingEvents.ChooseAddressError("Выберите адрес!"))
//                return@launch
//            }
//
//            val selectedDate = state.data.selectedDate
//
//            if (selectedDate == null) {
//                eventListener.emit(OrderingEvents.ChooseDateError("Выберите дату!"))
//                return@launch
//            }
//
//            if (state.data.checkDeliveryValue == 0) {
//                eventListener.emit(OrderingEvents.ChooseCheckDeliveryError)
//                return@launch
//            }
//
//            val needCall = when (state.data.needOperatorCall) {
//                true -> "Y"
//                false -> "N"
//            }
//            val useScore = when (state.data.usePersonalScore) {
//                true -> "Y"
//                false -> "N"
//            }
//
//            val totalPrice = state.data.total ?: return@launch
//            val depositPrice = state.data.deposit ?: return@launch
//
//            flow {
//                emit(
//                    repository.regOrder(
//                        orderType = 1 /*order type*/,
//                        device = deviceInfo,
//                        addressId = addressId,
//                        date = dateFormatter.format(selectedDate),
//                        paymentId = state.data.selectedPayMethodUI?.id,
//                        needOperatorCall = needCall,
//                        needShippingAlert = state.data.selectedShippingAlertUI?.name,
//                        shippingAlertPhone = phoneForDriver,
//                        comment = comment,
//                        totalPrice = totalPrice,
//                        shippingId = state.data.shippingInfoBundleUI?.id,
//                        shippingPrice = state.data.shippingPrice,
//                        name = name,
//                        phone = phone,
//                        email = email,
//                        inn = inn,
//                        companyName = companyName,
//                        deposit = depositPrice,
//                        fastShippingPrice = state.data.shippingInfoBundleUI?.todayShippingPrice,
//                        extraShippingPrice = state.data.shippingInfoBundleUI?.extraShippingPrice,
//                        commonShippingPrice = state.data.shippingInfoBundleUI?.commonShippingPrice,
//                        coupon = coupon,
//                        shippingIntervalId = state.data.selectedShippingIntervalUI?.id,
//                        overMoney = if (inputCash == "") {
//                            0
//                        } else {
//                            inputCash.toInt()
//                        },
//                        parking = state.data.parkingPrice,
//                        userId = userId,
//                        appVersion = BuildConfig.VERSION_NAME,
//                        checkDeliveryValue = state.data.checkDeliveryValue,
//                        useScore = useScore
//                    )
//                )
//            }
//                .onEach { response ->
//                    when (response) {
//                        is ResponseEntity.Success -> {
//                            val data = response.data.mapToUI()
//                            cartManager.clearCart()
//                            cartManager.updateCartListState(true)
//                            uiStateListener.value = state.copy(
//                                data = state.data.copy(
//                                    orderingCompletedInfoBundleUI = data
//                                ),
//                                loadingPage = false,
//                                error = null
//                            )
//                            accountManager.reportEvent("Заказ оформлен")
//                            eventListener.emit(OrderingEvents.OrderSuccess(data))
//                        }
//
//                        is ResponseEntity.Error -> {
//                            uiStateListener.value =
//                                state.copy(
//                                    loadingPage = false,
//                                    error = ErrorState.Error(response.errorMessage)
//                                )
//                        }
//
//                        else -> {}
//                    }
//                }
//                .flowOn(Dispatchers.Default)
//                .catch {
//                    debugLog { "reg order error ${it.localizedMessage}" }
//                    uiStateListener.value =
//                        state.copy(error = it.toErrorState(), loadingPage = false)
//                }
//                .collect()
//
//        }
//    }


    fun navigateBack() = viewModelScope.launch {
        eventListener.emit(OrderingEvents.GoBack)
    }

    fun navigateByRecipientItem(orderRecipientItem: OrderRecipientItemUi) = viewModelScope.launch {
        when (orderRecipientItem.id) {
            "adress" -> {
                eventListener.emit(OrderingEvents.GoToAddresses)
            }

            "klient" -> {

            }

            "time" -> {
                //todo - put actual address id
                eventListener.emit(OrderingEvents.GoToDeliveryDate(212504))
            }
        }
    }

    fun selectNotifyItem(notifyItem: OrderNotifyItemUi) = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(selectedNotifyItem = notifyItem)
        }
    }

    fun changeComment(field: FieldUi, updatedField: FieldUi) = viewModelScope.launch {
        uiStateListener.updateData { s ->
            if (s.comment == null) return@updateData s
            s.copy(
                comment = s.comment.copy(
                    value = updatedField.value
                )
            )
        }
    }

    fun navigateByPaymentItem(orderPaymentItem: OrderPaymentItemUi) = viewModelScope.launch {
        when (orderPaymentItem.id) {
            "oplata" -> {
                //todo - put actual address id and chosen local date
                eventListener.emit(OrderingEvents.GoToPaymentMethod(212504, LocalDate.now().plusDays(1)))
            }

            else -> {

            }
        }
    }

    fun doOrder() = viewModelScope.launch {

    }

    @Immutable
    data class OrderingState(
        val title: String = "",
        val comment: FieldUi? = null,
        val paymentSection: SectionUi<OrderPaymentItemUi> = SectionUi.empty(),
        val notifySection: SectionUi<OrderNotifyItemUi> = SectionUi.empty(),
        val selectedNotifyItem: OrderNotifyItemUi = OrderNotifyItemUi.Empty,
        val recipientSection: SectionUi<OrderRecipientItemUi> = SectionUi.empty(),
        val totals: List<OrderSummaryItemUi> = emptyList(),
        val button: ColorfulButtonUi = ColorfulButtonUi.Empty,
        val uiState: OrderingUiState = OrderingUiState.Loading,
    ) : State

    sealed class OrderingEvents : Event {
        data object GoBack : OrderingEvents()
        data object GoToAddresses : OrderingEvents()
        data class GoToDeliveryDate(val addressId: Int) : OrderingEvents()
        data class GoToPaymentMethod(val addressId: Int, val date: LocalDate) : OrderingEvents()
    }

    @Stable
    sealed interface OrderingUiState {
        data object Success : OrderingUiState
        data object Loading : OrderingUiState
        data object Error : OrderingUiState
    }
}