package com.m.vodovoz.feature.cart.ordering

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.R
import com.m.vodovoz.common.account.AccountManager
import com.m.vodovoz.common.cart.CartManager
import com.m.vodovoz.common.model.VodovozBoolean
import com.m.vodovoz.common.model.boolean
import com.m.vodovoz.common.model.from
import com.m.vodovoz.common.resources.ResourcesProvider
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.model.SectionUi
import com.m.vodovoz.design_system.model.VodovozPlaceholderUi
import com.m.vodovoz.design_system.model.order.OrderSummaryItemUi
import com.m.vodovoz.design_system.model.order.mapToUi
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.design_system.model.widgets.CheckboxUi
import com.m.vodovoz.design_system.model.widgets.FieldPopupWindowUi
import com.m.vodovoz.design_system.model.widgets.FieldUi
import com.m.vodovoz.design_system.model.widgets.toQueryMap
import com.m.vodovoz.domain.general.model.order.OrderingDetailsModel
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.addresses.model.AddressUi
import com.m.vodovoz.feature.cart.ordering.model.OrderNotifyItemUi
import com.m.vodovoz.feature.cart.ordering.model.OrderNotifySectionUi
import com.m.vodovoz.feature.cart.ordering.model.OrderingMenuItemUi
import com.m.vodovoz.feature.cart.ordering.model.OrderingUi
import com.m.vodovoz.feature.cart.ordering.model.clearItemErrors
import com.m.vodovoz.feature.cart.ordering.model.copyWithoutPayment
import com.m.vodovoz.feature.cart.ordering.model.mapToUi
import com.m.vodovoz.feature.cart.ordering.model.toIdAndValueMap
import com.m.vodovoz.feature.cart.ordering.model.toUi
import com.m.vodovoz.feature.cart.ordering.model.updateItemsByIds
import com.m.vodovoz.feature.delivery_date.model.DeliveryDateOptionUi
import com.m.vodovoz.feature.delivery_date.model.DeliveryTimeIntervalUi
import com.m.vodovoz.feature.delivery_date.model.displayDate
import com.m.vodovoz.feature.order_call_you.model.CallYouItemUi
import com.m.vodovoz.feature.payment_method.model.PaymentMethodItemUi
import com.m.vodovoz.feature.payment_method.model.intFieldValueOrZero
import com.m.vodovoz.feature.payment_method.model.noDigitsFieldValue
import com.m.vodovoz.ui.mvi.Event
import com.m.vodovoz.ui.mvi.MviViewModel
import com.m.vodovoz.ui.mvi.State
import com.m.vodovoz.ui.mvi.launchInViewModelScope
import com.m.vodovoz.util.extensions.singleResult
import com.m.vodovoz.util.formatters.VodovozDateFormatters
import com.m.vodovoz.util.isValidRussianPhoneNumber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.internal.toLongOrDefault
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
@Stable
class OrderingFlowViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    val tabManager: TabManager,
    val accountManager: AccountManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val resourcesProvider: ResourcesProvider,
    private val cartManager: CartManager
) : MviViewModel<OrderingFlowViewModel.OrderingState, OrderingFlowViewModel.OrderingEvents>(
    OrderingState()
) {
    private val coupon = savedStateHandle.get<String>("coupon")

    init {
        fetchOrderingDetails()
    }

    val actualIdAndValueMap: Map<String, String>
        get() = stateSnapshot.menus.toIdAndValueMap().filter { entry ->
            entry.key == OrderingDetailsModel.DOOR_MENU
                    || entry.key == OrderingDetailsModel.COMMENT_MENU
                    || entry.key == OrderingDetailsModel.CALL_YOU_MENU
        }

    private suspend fun fetchOrderingDetailsByState(): Result<OrderingDetailsModel> {
        return with(stateSnapshot.ordering) {
            vodovozServiceRepository.getOrderingDetails(
                coupon = coupon,
                addressId = addressId,
                date = date,
                timeInterval = timeInterval,
                useBonuses = paymentBonuses,
                bonuses = paymentBonusesValue,
                useBalance = paymentBalance,
                queryParams = actualIdAndValueMap
            ).singleResult()
        }
    }

    fun fetchOrderingDetails() = viewModelScope.launch {
        val orderingDetailsResult = fetchOrderingDetailsByState()

        orderingDetailsResult.onSuccess { orderingDetails ->
            updateState { s ->
                val addressMenuItem = s.getMenuById(OrderingDetailsModel.ADDRESS_MENU)

                val notifySection = orderingDetails.notifySection.toUi()
                s.copy(
                    title = orderingDetails.title,
                    paymentSection = orderingDetails.paymentSection.toUi { items ->
                        items.mapToUi()
                    },
                    recipientSection = orderingDetails.recipientSection.toUi { items ->
                        items.mapToUi().map { menuItemUi ->
                            if (menuItemUi.id == OrderingDetailsModel.ADDRESS_MENU && addressMenuItem != null) {
                                addressMenuItem
                            } else menuItemUi
                        }
                    },
                    notifySection = notifySection,
                    totals = orderingDetails.totals.mapToUi(),
                    button = orderingDetails.button.toUi(),
                    selectedNotifyItem = s.selectedNotifyItem.takeIf {
                        it != OrderNotifyItemUi.Empty
                    } ?: notifySection.options.firstOrNull() ?: s.selectedNotifyItem,
                    uiState = OrderingUiState.Order,
                    commentPopupWindow = orderingDetails.commentPopupWindow?.toUi()
                )
            }
            setPopupWindowCommentInMenu().join()
        }.onFailure {
            updateState { s ->
                s.copy(uiState = OrderingUiState.Error)
            }
        }
    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(OrderingEvents.GoBack)
    }

    fun navigateToHomeWithRefresh() = viewModelScope.launch {
        sendEvent(OrderingEvents.RefreshCart)
        sendEvent(OrderingEvents.GoToHome)
    }

    private fun updateRecipientSection(
        ids: List<String>,
        map: (OrderingMenuItemUi) -> OrderingMenuItemUi,
    ) {
        updateState { state ->
            with(state) {
                copy(
                    recipientSection = recipientSection.updateItemsByIds(
                        resetUntouchedErrors = true,
                        itemsIdsToTransform = ids,
                        transform = map
                    ),
                    paymentSection = paymentSection.clearItemErrors()
                )
            }
        }
    }

    fun handleRecipientItemClick(orderRecipientItem: OrderingMenuItemUi) = viewModelScope.launch {
        val ordering = stateSnapshot.ordering
        val addressId = ordering.addressId
        val timeInterval = ordering.timeInterval
        val date = ordering.date
        val earlierDelivery = VodovozBoolean.from(ordering.earlierDelivery?.second).boolean

        val queryParams = actualIdAndValueMap

        fun setAddressError() {
            updateRecipientSection(listOf(OrderingDetailsModel.ADDRESS_MENU)) { ui ->
                ui.copy(
                    error = true,
                    description = resourcesProvider.getString(R.string.data_not_filled_error)
                )
            }
        }

        suspend fun requireAddress(block: suspend (Long) -> Unit) {
            if (addressId == null) setAddressError() else block(addressId)
        }


        when (orderRecipientItem.id) {
            OrderingDetailsModel.ADDRESS_MENU -> {
                sendEvent(OrderingEvents.GoToAddresses(addressId))
            }

            OrderingDetailsModel.RECIPIENT_MENU -> requireAddress { id ->
                sendEvent(OrderingEvents.GoToOrderRecipient(id))
            }

            OrderingDetailsModel.DELIVERY_TIME_MENU -> requireAddress { id ->
                sendEvent(
                    OrderingEvents.GoToDeliveryDate(
                        id, date, timeInterval, earlierDelivery, queryParams
                    )
                )
            }

            OrderingDetailsModel.DOOR_MENU -> {
                updateState { s ->
                    s.copy(
                        recipientSection = s.recipientSection.updateItemsByIds(
                            itemsIdsToTransform = listOf(orderRecipientItem.id),
                            resetUntouchedErrors = false
                        ) { ui -> ui.copy(value = (!ui.value.toBoolean()).toString()) },
                        ordering = s.ordering.copyWithoutPayment()
                    )
                }

                fetchOrderingDetailsByState().onSuccess { orderingDetails ->
                    val callYouMenuItem =
                        stateSnapshot.getMenuById(OrderingDetailsModel.CALL_YOU_MENU)

                    updateState { s ->
                        s.copy(
                            paymentSection = orderingDetails.paymentSection.toUi {
                                it.mapToUi().map { itemUi ->
                                    if (callYouMenuItem != null && itemUi.id == callYouMenuItem.id) {
                                        callYouMenuItem
                                    } else {
                                        itemUi
                                    }
                                }
                            },
                        )
                    }
                }
            }

            OrderingDetailsModel.COMMENT_MENU -> requireAddress {
                updateState { s -> s.copy(showCommentBottomSheet = true) }
            }
        }
    }

    fun selectNotifyItem(notifyItem: OrderNotifyItemUi) = viewModelScope.launch {
        updateState { s ->
            s.copy(selectedNotifyItem = notifyItem)
        }
    }

    fun changeComment(field: FieldUi, updatedField: FieldUi) = viewModelScope.launch {
        updateState { s ->
            s.copy(
                commentPopupWindow = s.commentPopupWindow?.copy(field = updatedField)
            )
        }
    }

    fun navigateByPaymentItem(orderPaymentItem: OrderingMenuItemUi) = viewModelScope.launch {
        val ordering = stateSnapshot.ordering
        val addressId = ordering.addressId
        val timeInterval = ordering.timeInterval
        val date = ordering.date
        val callYouId = stateSnapshot.getMenuValueById(OrderingDetailsModel.CALL_YOU_MENU)
        val paymentMethodId = ordering.paymentId
        val balance = ordering.paymentBalance
        val bonuses = ordering.paymentBonuses
        val bonusesValue = ordering.paymentBonusesValue

        val queryParams = actualIdAndValueMap


        when (orderPaymentItem.id) {
            OrderingDetailsModel.PAYMENT_MENU -> {
                val recipientErrors = buildList {
                    if (addressId == null) {
                        add(OrderingDetailsModel.ADDRESS_MENU)
                    }
                    if (timeInterval == null || date == null) {
                        add(OrderingDetailsModel.DELIVERY_TIME_MENU)
                    }
                }

                updateRecipientSection(recipientErrors) { item ->
                    item.copy(
                        error = true,
                        description = resourcesProvider.getString(
                            R.string.data_not_filled_error
                        )
                    )
                }

                if (recipientErrors.isNotEmpty() || addressId == null || date == null) {
                    sendEvent(OrderingEvents.ScrollToTop)
                    return@launch
                } else {
                    val localDate = LocalDate.parse(date, VodovozDateFormatters.DMY)
                    sendEvent(
                        OrderingEvents.GoToPaymentMethod(
                            addressId = addressId,
                            date = localDate,
                            paymentMethodId = paymentMethodId,
                            balance = balance,
                            bonuses = bonuses,
                            bonusesValue = bonusesValue,
                            paymentChange = ordering.paymentChange,
                            queryParams = queryParams
                        )
                    )
                }

            }

            else -> {
                if (addressId == null) {
                    updateRecipientSection(listOf(OrderingDetailsModel.ADDRESS_MENU)) { item ->
                        item.copy(
                            error = true,
                            description = resourcesProvider.getString(
                                R.string.data_not_filled_error
                            )
                        )
                    }
                    sendEvent(OrderingEvents.ScrollToTop)
                } else {
                    sendEvent(OrderingEvents.GoToCallYou(addressId, callYouId, queryParams))
                }
            }
        }
    }

    fun doOrder(deviceInfo: String) = viewModelScope.launch {
        val ordering = stateSnapshot.ordering
        val earlierDelivery = ordering.earlierDelivery
        val idAndValueMap = stateSnapshot.menus.toIdAndValueMap().filter { entry ->
            entry.key == OrderingDetailsModel.DOOR_MENU
                    || entry.key == OrderingDetailsModel.COMMENT_MENU
                    || entry.key == OrderingDetailsModel.CALL_YOU_MENU
        }

        val callYouId = stateSnapshot.getMenuValueById(OrderingDetailsModel.CALL_YOU_MENU)

        if (
            ordering.addressId != null && ordering.date != null
            && ordering.timeInterval != null && ordering.recipientPhone != null
            && ordering.paymentId != null && callYouId != null && ordering.recipientName != null
        ) {
            updateState { s ->
                s.copy(button = s.button.copy(loading = true))
            }


            val params = mapOf(earlierDelivery ?: Pair("", "")) + with(stateSnapshot) {
                val extraPhoneField = notifySection.extraPhoneField
                val extraPhoneMap =
                    if (extraPhoneField != null && extraPhoneField.value.isValidRussianPhoneNumber()) {
                        extraPhoneField.toQueryMap()
                    } else emptyMap()

                extraPhoneMap + totals.associate { it.id to it.value } + idAndValueMap
            }


            vodovozServiceRepository.doOrder(
                addressId = ordering.addressId,
                deliveryDate = ordering.date,
                deliveryTimeInterval = ordering.timeInterval,
                userFIO = ordering.recipientName,
                userPhone = ordering.recipientPhone,
                userEmail = ordering.recipientEmail,
                paymentMethodId = ordering.paymentId.toLongOrDefault(0),
                paymentChange = ordering.paymentChange,
                coupon = coupon,
                deviceInfo = deviceInfo,
                notifyDriverId = stateSnapshot.selectedNotifyItem?.value,
                useBonuses = ordering.paymentBonuses,
                useBalance = ordering.paymentBalance,
                bonuses = ordering.paymentBonusesValue,
                params = params
            ).singleResult().onSuccess { placeholder ->
                updateState { s ->
                    s.copy(uiState = OrderingUiState.Success(placeholder.toUi()))
                }
                cartManager.clearCart()
                sendEvent(OrderingEvents.UpdateBottomCart)
            }

            updateState { s ->
                s.copy(button = s.button.copy(loading = false))
            }

        } else {
            validateOrderingDetails()
        }
    }

    private fun validateOrderingDetails() = viewModelScope.launch {
        val ordering = stateSnapshot.ordering
        val recipientErrors = buildList {
            if (ordering.addressId == null) add(OrderingDetailsModel.ADDRESS_MENU)
            if (ordering.recipientName == null || ordering.recipientPhone == null) add(
                OrderingDetailsModel.RECIPIENT_MENU
            )
            if (ordering.timeInterval == null || ordering.date == null) add(OrderingDetailsModel.DELIVERY_TIME_MENU)
        }

        val paymentErrors = buildList {
            if (ordering.paymentId == null) add(OrderingDetailsModel.PAYMENT_MENU)
            if (stateSnapshot.getMenuValueById(OrderingDetailsModel.CALL_YOU_MENU) == null) add(
                OrderingDetailsModel.CALL_YOU_MENU
            )
        }

        updateState { s ->
            s.copy(
                recipientSection = s.recipientSection.updateItemsByIds(
                    itemsIdsToTransform = recipientErrors,
                    resetUntouchedErrors = true,
                    transform = { menuItem ->
                        menuItem.copy(
                            error = true,
                            description = resourcesProvider.getString(R.string.data_not_filled_error)
                        )
                    }
                ),
                paymentSection = s.paymentSection.updateItemsByIds(
                    itemsIdsToTransform = paymentErrors,
                    resetUntouchedErrors = true,
                    transform = { menuItem ->
                        menuItem.copy(
                            error = true,
                            description = resourcesProvider.getString(R.string.data_not_filled_error)
                        )
                    }
                )
            )
        }

        if (recipientErrors.isNotEmpty()) {
            sendEvent(OrderingEvents.ScrollToTop)
        } else if (paymentErrors.isNotEmpty()) {
            sendEvent(OrderingEvents.ScrollToBottom)
        }
    }

    fun refreshRecipient() = viewModelScope.launch {
        updateState { s ->
            s.copy(showRefreshIndicator = true)
        }

        fetchRecipient().join()


        updateState { s ->
            s.copy(showRefreshIndicator = false)
        }

    }

    private fun fetchRecipient() = viewModelScope.launch {
        val addressId = stateSnapshot.ordering.addressId ?: return@launch
        val recipientResult = vodovozServiceRepository.getRecipient(addressId).singleResult()

        recipientResult.onSuccess { recipientModel ->

            updateState { s ->
                val updatedOrdering = s.ordering.copy(
                    recipientPhone = recipientModel.phone.takeIf { it.isNotBlank() },
                    recipientName = recipientModel.fio.takeIf { it.isNotBlank() }
                )

                s.copy(
                    ordering = updatedOrdering,
                    recipientSection = s.recipientSection.updateItemsByIds(
                        resetUntouchedErrors = true,
                        itemsIdsToTransform = listOf(OrderingDetailsModel.RECIPIENT_MENU),
                        transform = {
                            it.copy(
                                name = recipientModel.fio,
                                description = recipientModel.phone,
                                error = false,
                            )
                        }
                    )
                )
            }
        }.onFailure {}
    }

    fun setAddress(address: AddressUi) = viewModelScope.launch {
        updateState { s ->
            s.copy(
                ordering = OrderingUi.Empty.copy(addressId = address.id),
                recipientSection = s.recipientSection.updateItemsByIds(
                    resetUntouchedErrors = false,
                    itemsIdsToTransform = listOf(OrderingDetailsModel.ADDRESS_MENU),
                    transform = {
                        it.copy(
                            error = false,
                            name = address.address,
                            description = address.description
                        )
                    }
                ),
                showRefreshIndicator = true)
        }

        fetchOrderingDetails().join()
        refreshRecipient().join()
    }

    fun setDeliveryDateTime(
        timeInterval: DeliveryTimeIntervalUi,
        date: DeliveryDateOptionUi,
    ) = viewModelScope.launch {
        updateState { s ->
            s.copy(
                ordering = s.ordering.copyWithoutPayment().copy(
                    timeInterval = timeInterval.value,
                    date = date.value,
                ),
                recipientSection = s.recipientSection.updateItemsByIds(
                    resetUntouchedErrors = false,
                    itemsIdsToTransform = listOf(OrderingDetailsModel.DELIVERY_TIME_MENU),
                    transform = { itemUi ->
                        val displayDate = date.displayDate(
                            resourcesProvider.getString(R.string.today),
                            resourcesProvider.getString(R.string.tomorrow)
                        )
                        itemUi.copy(
                            error = false,
                            name = resourcesProvider.getString(
                                R.string.date_time_inteval,
                                displayDate, timeInterval.name
                            ),
                            description = resourcesProvider.getString(R.string.delivery)
                        )
                    }
                ),
                showRefreshIndicator = true
            )
        }

        fetchOrderingDetailsByState().onSuccess { orderingDetails ->
            updateState { s ->
                s.copy(
                    paymentSection = s.paymentSection.copy(
                        items = s.paymentSection.items.map { menuItem ->
                            if (menuItem.id == OrderingDetailsModel.PAYMENT_MENU) {
                                orderingDetails.paymentSection.items.mapToUi()
                                    .find { it.id == OrderingDetailsModel.PAYMENT_MENU } ?: menuItem
                            } else menuItem
                        }
                    ),
                    totals = orderingDetails.totals.mapToUi()
                )
            }
        }

        updateState { s ->
            s.copy(showRefreshIndicator = false)
        }
    }

    fun setCallYou(callYouItem: CallYouItemUi) = viewModelScope.launch {
        updateState { s ->
            s.copy(
                paymentSection = s.paymentSection.updateItemsByIds(
                    resetUntouchedErrors = false,
                    itemsIdsToTransform = listOf(OrderingDetailsModel.CALL_YOU_MENU),
                    transform = { itemUi ->
                        itemUi.copy(
                            error = false,
                            description = callYouItem.name,
                            value = callYouItem.value
                        )
                    }
                )
            )
        }
    }

    fun setPaymentInfo(
        paymentBalance: PaymentMethodItemUi?,
        paymentBonuses: PaymentMethodItemUi?,
        paymentMethod: PaymentMethodItemUi?,
    ) = launchInViewModelScope {
        if (
            paymentBalance == null
            && paymentBonuses == null
            && paymentMethod == null
        ) return@launchInViewModelScope

        val updated = stateSnapshot
            .let { state ->
                paymentBalance?.let {
                    state.copy(
                        ordering = state.ordering.copy(
                            paymentBalance = it.value
                        )
                    )
                } ?: state
            }
            .let { state ->
                paymentBonuses?.let {
                    state.copy(
                        ordering = state.ordering.copy(
                            paymentBonuses = it.value,
                            paymentBonusesValue = it.intFieldValueOrZero
                        )
                    )
                } ?: state
            }
            .let { state ->
                paymentMethod?.let { method ->
                    state.copy(
                        ordering = state.ordering.copy(
                            paymentId = method.id,
                            paymentChange = method.noDigitsFieldValue
                        ),
                        paymentSection = state.paymentSection.updateItemsByIds(
                            resetUntouchedErrors = false,
                            itemsIdsToTransform = listOf(OrderingDetailsModel.PAYMENT_MENU),
                            transform = { orderingMenu ->
                                orderingMenu.copy(
                                    error = false,
                                    image = method.image,
                                    name = method.name,
                                    description = resourcesProvider.getString(
                                        R.string.payment_method
                                    )
                                )
                            }
                        )
                    )
                } ?: state
            }

        updateState { updated }

        fetchOrderingDetailsByState().onSuccess { orderingDetailsModel ->
            updateState { state ->
                state.copy(
                    totals = orderingDetailsModel.totals.mapToUi(),
                    button = orderingDetailsModel.button.toUi()
                )
            }
        }
    }

    fun activatePayButton(button: ColorfulButtonUi?) = viewModelScope.launch {
        if (button?.url == null) {
            navigateToHomeWithRefresh()
            return@launch
        }

        sendEvent(OrderingEvents.RefreshCart)

        if (button.browser == false) {
            sendEvent(OrderingEvents.GoToWebView(button.url))
        } else {
            sendEvent(OrderingEvents.OpenUrl(button.url))
        }
    }

    fun setEarlierDelivery(checkbox: CheckboxUi) = viewModelScope.launch {
        updateState { s ->
            s.copy(
                ordering = s.ordering.copy(earlierDelivery = checkbox.id to checkbox.value())
            )
        }
    }

    fun resetOrderIfEmptyAddress(address: AddressUi) = launchInViewModelScope {
        if (address != AddressUi.Empty) return@launchInViewModelScope

        updateState { OrderingState() }

        fetchOrderingDetails().join()
    }

    fun changeExtraPhoneField(field: FieldUi, updatedField: FieldUi) = launchInViewModelScope {
        updateState { s ->
            s.copy(
                notifySection = s.notifySection.copy(
                    extraPhoneField = updatedField
                )
            )
        }
    }

    fun hideCommentBottomSheet() = launchInViewModelScope {
        updateState { state ->

            val commentPopupWindow = state.commentPopupWindow
            val currentValue = state.getMenuById(OrderingDetailsModel.COMMENT_MENU)?.value.orEmpty()

            state.copy(
                showCommentBottomSheet = false,
                commentPopupWindow = commentPopupWindow?.copy(
                    field = commentPopupWindow.field.copy(value = currentValue)
                )
            )
        }
    }

    fun setPopupWindowCommentInMenu() = launchInViewModelScope {
        updateState { state ->
            state.copy(
                showCommentBottomSheet = false,
                recipientSection = state.recipientSection.updateItemsByIds(
                    itemsIdsToTransform = listOf(OrderingDetailsModel.COMMENT_MENU),
                    resetUntouchedErrors = false,
                    transform = {
                        state.commentPopupWindow?.field?.let { field ->
                            val newValue = field.value
                            it.copy(value = newValue, description = newValue.ifEmpty { field.hint })
                        } ?: it
                    }
                )
            )
        }
    }

    @Immutable
    data class OrderingState(
        val title: String = "",
        val paymentSection: SectionUi<OrderingMenuItemUi> = SectionUi.empty(),
        val notifySection: OrderNotifySectionUi = OrderNotifySectionUi.Empty,
        val selectedNotifyItem: OrderNotifyItemUi? = null,
        val recipientSection: SectionUi<OrderingMenuItemUi> = SectionUi.empty(),
        val totals: List<OrderSummaryItemUi> = emptyList(),
        val button: ColorfulButtonUi = ColorfulButtonUi.Empty,
        val uiState: OrderingUiState = OrderingUiState.Loading,
        val showRefreshIndicator: Boolean = false,
        val ordering: OrderingUi = OrderingUi.Empty,
        val commentPopupWindow: FieldPopupWindowUi? = null,
        val showCommentBottomSheet: Boolean = false
    ) : State {

        val menus: List<OrderingMenuItemUi> get() = paymentSection.items + recipientSection.items

        fun getMenuById(id: String): OrderingMenuItemUi? {
            return menus.firstOrNull { it.id == id }
        }

        fun getMenuValueById(id: String): String? {
            return menus.firstOrNull { it.id == id }?.value
        }

    }

    sealed class OrderingEvents : Event {
        data object GoBack : OrderingEvents()
        data object GoToHome : OrderingEvents()
        data class GoToAddresses(val addressId: Long?) : OrderingEvents()
        data object ScrollToTop : OrderingEvents()
        data object RefreshCart : OrderingEvents()
        data object ScrollToBottom : OrderingEvents()
        data object UpdateBottomCart : OrderingEvents()

        sealed class OrderingEventsWithQueryParams : OrderingEvents() {

            abstract val queryParams: Map<String, String>

        }

        data class GoToDeliveryDate(
            val addressId: Long,
            val date: String?,
            val timeInterval: String?,
            val earlierDelivery: Boolean,
            override val queryParams: Map<String, String>
        ) : OrderingEventsWithQueryParams()

        data class GoToPaymentMethod(
            val addressId: Long,
            val date: LocalDate,
            val paymentMethodId: String?,
            val balance: Boolean?,
            val bonuses: Boolean?,
            val bonusesValue: Int?,
            val paymentChange: String?,
            override val queryParams: Map<String, String>
        ) : OrderingEventsWithQueryParams()

        data class GoToOrderRecipient(val addressId: Long) : OrderingEvents()
        data class GoToCallYou(
            val addressId: Long,
            val callYouId: String?,
            override val queryParams: Map<String, String>
        ) : OrderingEventsWithQueryParams()

        data class OpenUrl(val url: String) : OrderingEvents()
        data class GoToWebView(val url: String) : OrderingEvents()
    }

    @Stable
    sealed interface OrderingUiState {
        data object Order : OrderingUiState
        data object Loading : OrderingUiState
        data object Error : OrderingUiState
        data class Success(val placeholder: VodovozPlaceholderUi) : OrderingUiState
    }
}
