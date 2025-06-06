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
import com.vodovoz.app.data.config.ShippingAlertConfig
import com.vodovoz.app.data.model.common.ResponseEntity
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.SectionUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.design_system.model.widgets.FieldUi
import com.vodovoz.app.design_system.model.widgets.toUi
import com.vodovoz.app.domain.general.model.cart.OrderSummaryItemUi
import com.vodovoz.app.domain.general.model.cart.mapToUi
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.cart.ordering.model.OrderNotifyItemUi
import com.vodovoz.app.feature.cart.ordering.model.OrderPaymentItemUi
import com.vodovoz.app.feature.cart.ordering.model.OrderRecipientItemUi
import com.vodovoz.app.feature.cart.ordering.model.mapToUi
import com.vodovoz.app.mapper.FreeShippingDaysInfoBundleMapper.mapToUI
import com.vodovoz.app.mapper.OrderingCompletedInfoBundleMapper.mapToUI
import com.vodovoz.app.mapper.ShippingInfoBundleMapper.mapToUI
import com.vodovoz.app.ui.model.AddressUI
import com.vodovoz.app.ui.model.FreeShippingDaysInfoBundleUI
import com.vodovoz.app.ui.model.PayMethodUI
import com.vodovoz.app.ui.model.ShippingAlertUI
import com.vodovoz.app.ui.model.ShippingInfoBundleUI
import com.vodovoz.app.ui.model.ShippingIntervalUI
import com.vodovoz.app.ui.model.custom.OrderingCompletedInfoBundleUI
import com.vodovoz.app.util.extensions.debugLog
import com.vodovoz.app.util.extensions.getDeviceInfo
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
@Stable
class OrderingFlowViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MainRepository,
    private val accountManager: AccountManager,
    private val application: Application,
    private val cartManager: CartManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
) : PagingContractViewModel<OrderingFlowViewModel.OrderingState, OrderingFlowViewModel.OrderingEvents>(
    OrderingState(
        full = savedStateHandle.get<Int>("full"),
        deposit = savedStateHandle.get<Int>("deposit"),
        discount = savedStateHandle.get<Int>("discount"),
        total = savedStateHandle.get<Int>("total")
    )
) {

    override val eventListener = MutableSharedFlow<OrderingEvents>(
        replay = 0,
        extraBufferCapacity = 1,
        BufferOverflow.DROP_OLDEST
    )

    private val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    private val coupon = savedStateHandle.get<String>("coupon") ?: ""

    init {
        debugLog { "full ${state.data.full}, discount ${state.data.discount}" }
        fetchFreeShippingDaysInfo()
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

    fun regOrder(
        comment: String = "",
        name: String = "",
        phone: String = "",
        email: String = "",
        companyName: String = "",
        inn: String = "",
        inputCash: String = "",
        phoneForDriver: String = "",
    ) {
        viewModelScope.launch {

            val userId = accountManager.fetchAccountId() ?: return@launch
            val deviceInfo = application.getDeviceInfo()
            val addressId = state.data.selectedAddressUI?.id
            if (addressId == null) {
                eventListener.emit(OrderingEvents.ChooseAddressError("Выберите адрес!"))
                return@launch
            }

            val selectedDate = state.data.selectedDate

            if (selectedDate == null) {
                eventListener.emit(OrderingEvents.ChooseDateError("Выберите дату!"))
                return@launch
            }

            if (state.data.selectedShippingIntervalUI == null) {
                eventListener.emit(OrderingEvents.ChooseIntervalError("Выберите интервал!"))
                return@launch
            }

            if (state.data.selectedPayMethodUI == null) {
                eventListener.emit(OrderingEvents.ChoosePayMethodError("Выберите способ оплаты!"))
                return@launch
            }

            if (state.data.checkDeliveryValue == 0) {
                eventListener.emit(OrderingEvents.ChooseCheckDeliveryError)
                return@launch
            }

            val needCall = when (state.data.needOperatorCall) {
                true -> "Y"
                false -> "N"
            }
            val useScore = when (state.data.usePersonalScore) {
                true -> "Y"
                false -> "N"
            }

            val totalPrice = state.data.total ?: return@launch
            val depositPrice = state.data.deposit ?: return@launch

            flow {
                emit(
                    repository.regOrder(
                        orderType = state.data.selectedOrderType.value,
                        device = deviceInfo,
                        addressId = addressId,
                        date = dateFormatter.format(selectedDate),
                        paymentId = state.data.selectedPayMethodUI?.id,
                        needOperatorCall = needCall,
                        needShippingAlert = state.data.selectedShippingAlertUI?.name,
                        shippingAlertPhone = phoneForDriver,
                        comment = comment,
                        totalPrice = totalPrice,
                        shippingId = state.data.shippingInfoBundleUI?.id,
                        shippingPrice = state.data.shippingPrice,
                        name = name,
                        phone = phone,
                        email = email,
                        inn = inn,
                        companyName = companyName,
                        deposit = depositPrice,
                        fastShippingPrice = state.data.shippingInfoBundleUI?.todayShippingPrice,
                        extraShippingPrice = state.data.shippingInfoBundleUI?.extraShippingPrice,
                        commonShippingPrice = state.data.shippingInfoBundleUI?.commonShippingPrice,
                        coupon = coupon,
                        shippingIntervalId = state.data.selectedShippingIntervalUI?.id,
                        overMoney = if (inputCash == "") {
                            0
                        } else {
                            inputCash.toInt()
                        },
                        parking = state.data.parkingPrice,
                        userId = userId,
                        appVersion = BuildConfig.VERSION_NAME,
                        checkDeliveryValue = state.data.checkDeliveryValue,
                        useScore = useScore
                    )
                )
            }
                .onEach { response ->
                    when (response) {
                        is ResponseEntity.Success -> {
                            val data = response.data.mapToUI()
                            cartManager.clearCart()
                            cartManager.updateCartListState(true)
                            uiStateListener.value = state.copy(
                                data = state.data.copy(
                                    orderingCompletedInfoBundleUI = data
                                ),
                                loadingPage = false,
                                error = null
                            )
                            accountManager.reportEvent("Заказ оформлен")
                            eventListener.emit(OrderingEvents.OrderSuccess(data))
                        }

                        is ResponseEntity.Error -> {
                            uiStateListener.value =
                                state.copy(
                                    loadingPage = false,
                                    error = ErrorState.Error(response.errorMessage)
                                )
                        }

                        else -> {}
                    }
                }
                .flowOn(Dispatchers.Default)
                .catch {
                    debugLog { "reg order error ${it.localizedMessage}" }
                    uiStateListener.value =
                        state.copy(error = it.toErrorState(), loadingPage = false)
                }
                .collect()

        }
    }

    fun checkAddress() {
        viewModelScope.launch {
            val addressId = state.data.selectedAddressUI?.id
            if (addressId == null) {
                eventListener.emit(OrderingEvents.ChooseAddressError("Выберите адрес!"))
                return@launch
            }
        }
    }

    fun setSelectedDate(date: LocalDate) {
        uiStateListener.value = state.copy(
            data = state.data.copy(
                selectedDate = date
            )
        )
    }

    fun setSelectedShippingInterval(itemId: Long) {
        val interval =
            state.data.shippingInfoBundleUI?.shippingIntervalUIList?.find { it.id == itemId }
                ?: return

        uiStateListener.value = state.copy(
            data = state.data.copy(
                selectedShippingIntervalUI = interval
            )
        )
    }

    fun setSelectedShippingAlert(itemId: Long) {
        val alert = state.data.shippingAlertList.find { it.id == itemId } ?: return
        uiStateListener.value = state.copy(
            data = state.data.copy(
                selectedShippingAlertUI = alert
            )
        )
    }

    fun clearShippingAlert() {
        uiStateListener.value = state.copy(
            data = state.data.copy(
                selectedShippingAlertUI = null
            )
        )
    }

    fun setNeedOperatorCall(bool: Boolean) {
        uiStateListener.value = state.copy(
            data = state.data.copy(
                needOperatorCall = bool
            )
        )
    }

    fun setUsePersonalScore(bool: Boolean) {
        uiStateListener.value = state.copy(
            data = state.data.copy(
                usePersonalScore = bool
            )
        )
    }

    fun setSelectedAddress(item: AddressUI) {
        uiStateListener.value = state.copy(
            data = state.data.copy(
                selectedAddressUI = item
            )
        )
        fetchShippingInfo()
    }

    fun setSelectedPaymentMethod(itemId: Long) {
        val method =
            state.data.shippingInfoBundleUI?.payMethodUIList?.find { it.id == itemId } ?: return
        uiStateListener.value = state.copy(
            data = state.data.copy(
                selectedPayMethodUI = method
            )
        )
    }

    fun setSelectedOrderType(type: OrderType) {
        viewModelScope.launch {
            if (state.data.selectedOrderType == type) return@launch
            eventListener.emit(OrderingEvents.ClearFields)
            uiStateListener.value = state.copy(
                data = OrderingState(
                    selectedOrderType = type,
                    total = state.data.total,
                    full = state.data.full,
                    deposit = state.data.deposit,
                    discount = state.data.discount
                )
            )
        }
    }

    fun fetchShippingInfo(
        pay: Boolean = false,
        intervals: Boolean = false,
        today: Boolean = false,
    ) {
        viewModelScope.launch {
            val address = state.data.selectedAddressUI
            if (address == null) {
                eventListener.emit(OrderingEvents.OrderingErrorInfo("Выберите адрес!"))
                return@launch
            }

            val selectedDate = state.data.selectedDate

//            if (selectedDate == null) {
//                eventListener.emit(OrderingEvents.OrderingErrorInfo("Выберите дату!"))
//                return@launch
//            }

            val userId = accountManager.fetchAccountId() ?: return@launch


            flow {
                emit(
                    repository.fetchShippingInfo(
                        addressId = address.id,
                        userId = userId,
                        date = if (selectedDate != null) dateFormatter.format(selectedDate) else null,
                        appVersion = BuildConfig.VERSION_NAME
                    )
                )
            }
                .onEach { response ->
                    if (response is ResponseEntity.Success) {
                        val data = response.data.mapToUI()
                        val full = state.data.full
                        val diposit = state.data.deposit
                        val discount = state.data.discount
                        val total = state.data.total
                        var newTotal = if (full != null && discount != null && total != null) {
                            full - discount + data.shippingPrice
                        } else {
                            state.data.total
                        }
                        if (diposit != null && newTotal != null) {
                            newTotal += diposit
                        }
                        uiStateListener.value = state.copy(
                            data = state.data.copy(
                                total = newTotal,
                                shippingInfoBundleUI = data,
                                shippingPrice = data.shippingPrice,
                                parkingPrice = data.parkingPrice,
                                payMethodList = data.payMethodUIList,
                                shippingIntervalUiList = data.shippingIntervalUIList,
                                todayShippingInfo = data.todayShippingInfo.takeIf { it.isNotEmpty() }
                            ),
                            loadingPage = false,
                            error = null
                        )
                        if (pay) {
                            eventListener.emit(
                                OrderingEvents.ShowPaymentMethod(
                                    data.payMethodUIList,
                                    state.data.selectedPayMethodUI?.id
                                )
                            )
                        }
                        if (intervals) {
                            eventListener.emit(
                                OrderingEvents.ShowShippingIntervals(
                                    data.shippingIntervalUIList,
                                    state.data.selectedDate
                                )
                            )
                        }
                        if (today) {
                            eventListener.emit(OrderingEvents.TodayShippingMessage(data.todayShippingInfo))
                        }
                    } else {
                        uiStateListener.value =
                            state.copy(
                                loadingPage = false,
                                error = ErrorState.Error()
                            )
                    }
                }
                .flowOn(Dispatchers.Default)
                .catch {
                    debugLog { "fetch shipping info error ${it.localizedMessage}" }
                    uiStateListener.value =
                        state.copy(error = it.toErrorState(), loadingPage = false)
                }
                .collect()
        }
    }

    private fun fetchFreeShippingDaysInfo() {
        viewModelScope.launch {
            uiStateListener.value = state.copy(loadingPage = true)
            flow { emit(repository.fetchFreeShippingDaysInfoResponse(appVersion = BuildConfig.VERSION_NAME)) }
                .onEach { response ->
                    if (response is ResponseEntity.Success) {
                        val data = response.data.mapToUI()
                        uiStateListener.value = state.copy(
                            data = state.data.copy(
                                shippingDaysInfoBundleUi = data
                            ),
                            loadingPage = false,
                            error = null
                        )
                    } else {
                        uiStateListener.value =
                            state.copy(
                                loadingPage = false,
                                error = ErrorState.Error()
                            )
                    }
                }
                .flowOn(Dispatchers.Default)
                .catch {
                    debugLog { "fetch free shipping days error ${it.localizedMessage}" }
                    uiStateListener.value =
                        state.copy(error = it.toErrorState(), loadingPage = false)
                }
                .collect()
        }
    }

    fun onAddressBtnClick() {
        viewModelScope.launch {
            eventListener.emit(OrderingEvents.OnAddressBtnClick(state.data.selectedOrderType.name))
        }
    }

    fun onFreeShippingClick() {
        viewModelScope.launch {
            val bundle = state.data.shippingDaysInfoBundleUi
            if (bundle == null) {
                fetchFreeShippingDaysInfo()
            } else {
                eventListener.emit(OrderingEvents.OnFreeShippingClick(bundle))
            }
        }
    }

    fun onDateClick() {
        viewModelScope.launch {
            if (state.data.selectedAddressUI == null) {
                eventListener.emit(OrderingEvents.OrderingErrorInfo("Выберите адрес!"))
            } else {
                eventListener.emit(OrderingEvents.ShowDatePicker)
            }
        }
    }

    fun onShippingAlertClick() {
        viewModelScope.launch {
            eventListener.emit(OrderingEvents.OnShippingAlertClick(state.data.shippingAlertList))
        }
    }

    fun onCheckDeliveryClick() {
        viewModelScope.launch {
            eventListener.emit(
                OrderingEvents.ShowCheckDeliveryBs(
                    state.data.checkDeliveryValue,
                    state.data.shippingInfoBundleUI?.isNewUser ?: false
                )
            )
        }
    }

    fun setCheckDelivery(value: Int) {
        uiStateListener.value = state.copy(
            data = state.data.copy(
                checkDeliveryValue = value
            )
        )
    }

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

            }

            else -> {

            }
        }
    }

    fun doOrder() = viewModelScope.launch {

    }

    @Immutable
    data class OrderingState(
        val full: Int? = null,
        val deposit: Int? = null,
        val discount: Int? = null,
        val total: Int? = null,
        val selectedAddressUI: AddressUI? = null,
        val selectedDate: LocalDate? = null,
        val selectedOrderType: OrderType = OrderType.PERSONAL,
        val selectedPayMethodUI: PayMethodUI? = null,
        val selectedShippingIntervalUI: ShippingIntervalUI? = null,
        val selectedShippingAlertUI: ShippingAlertUI? = null,
        val needOperatorCall: Boolean = false,
        val usePersonalScore: Boolean = false,
        val shippingPrice: Int? = null,
        val parkingPrice: Int? = null,
        val payMethodList: List<PayMethodUI>? = null,
        val todayShippingInfo: String? = null,
        val shippingInfoBundleUI: ShippingInfoBundleUI? = null,
        val shippingIntervalUiList: List<ShippingIntervalUI>? = null,
        val shippingAlertList: List<ShippingAlertUI> = ShippingAlertConfig.shippingAlertEntityListUI,
        val shippingDaysInfoBundleUi: FreeShippingDaysInfoBundleUI? = null,
        val orderingCompletedInfoBundleUI: OrderingCompletedInfoBundleUI? = null,
        val checkDeliveryValue: Int = 0,

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
        data class OrderingErrorInfo(val message: String) : OrderingEvents()
        data class OrderSuccess(val item: OrderingCompletedInfoBundleUI) : OrderingEvents()

        data class OnAddressBtnClick(val typeName: String) : OrderingEvents()
        data class OnFreeShippingClick(val bundle: FreeShippingDaysInfoBundleUI) : OrderingEvents()
        data object ShowDatePicker : OrderingEvents()
        data class OnShippingAlertClick(val list: List<ShippingAlertUI>) : OrderingEvents()

        data class ShowPaymentMethod(val list: List<PayMethodUI>, val selectedPayMethodId: Long?) :
            OrderingEvents()

        data class ShowShippingIntervals(
            val list: List<ShippingIntervalUI>,
            val selectedDate: LocalDate?,
        ) : OrderingEvents()

        data class TodayShippingMessage(val message: String) : OrderingEvents()

        data class ChooseAddressError(val message: String) : OrderingEvents()
        data class ChooseDateError(val message: String) : OrderingEvents()
        data class ChooseIntervalError(val message: String) : OrderingEvents()
        data class ChoosePayMethodError(val message: String) : OrderingEvents()
        data object ChooseCheckDeliveryError : OrderingEvents()

        data class ShowCheckDeliveryBs(val value: Int, val isNewUser: Boolean = false) :
            OrderingEvents()

        data object ClearFields : OrderingEvents()



        data object GoBack : OrderingEvents()
        data object GoToAddresses: OrderingEvents()
        data class GoToDeliveryDate(val addressId: Long): OrderingEvents()
    }

    @Immutable
    sealed interface OrderingUiState {
        data object Success : OrderingUiState
        data object Loading : OrderingUiState
        data object Error : OrderingUiState
    }
}