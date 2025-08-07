package com.vodovoz.app.feature.cart.ordering

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.R
import com.vodovoz.app.common.content.Event
import com.vodovoz.app.common.content.PagingContractViewModel
import com.vodovoz.app.common.content.State
import com.vodovoz.app.common.content.updateData
import com.vodovoz.app.common.model.VodovozBoolean
import com.vodovoz.app.common.model.from
import com.vodovoz.app.common.resources.ResourcesProvider
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.SectionUi
import com.vodovoz.app.design_system.model.VodovozPlaceholderUi
import com.vodovoz.app.design_system.model.order.OrderSummaryItemUi
import com.vodovoz.app.design_system.model.order.mapToUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.design_system.model.widgets.CheckboxUi
import com.vodovoz.app.design_system.model.widgets.FieldUi
import com.vodovoz.app.design_system.model.widgets.toUi
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.addresses.model.AddressUi
import com.vodovoz.app.feature.cart.ordering.model.OrderNotifyItemUi
import com.vodovoz.app.feature.cart.ordering.model.OrderingMenuItemUi
import com.vodovoz.app.feature.cart.ordering.model.OrderingUi
import com.vodovoz.app.feature.cart.ordering.model.mapToUi
import com.vodovoz.app.feature.delivery_date.model.DeliveryDateOptionUi
import com.vodovoz.app.feature.delivery_date.model.DeliveryTimeIntervalUi
import com.vodovoz.app.feature.order_call_you.model.CallYouItemUi
import com.vodovoz.app.feature.payment_method.model.PaymentMethodItemUi
import com.vodovoz.app.util.extensions.singleResult
import com.vodovoz.app.util.formatters.VodovozDateFormatters
import com.vodovoz.app.util.toIntRoundOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
@Stable
class OrderingFlowViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val resourcesProvider: ResourcesProvider,
) : PagingContractViewModel<OrderingFlowViewModel.OrderingState, OrderingFlowViewModel.OrderingEvents>(
    OrderingState()
) {
    private val coupon = savedStateHandle.get<String>("coupon")

    companion object {
        private const val ADDRESS_MENU_ID = "adress"
        private const val RECIPIENT_MENU_ID = "klient"
        private const val DELIVERY_TIME_MENU_ID = "time"
        private const val PAYMENT_MENU_ID = "oplata"
        private const val CALL_YOU_MENU_ID = "vampozvonit"
    }

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
                    comment = s.comment ?: orderingDetails.commentField?.toUi(),
                    paymentSection = orderingDetails.paymentSection.toUi { items ->
                        items.mapToUi()
                    },
                    recipientSection = orderingDetails.recipientSection.toUi { items ->
                        items.mapToUi().map { menuItemUi ->
                            if (menuItemUi.id == CALL_YOU_MENU_ID && menuItemUi.defaultValue != null) {
                                uiStateListener.updateData { s ->
                                    s.copy(
                                        ordering = s.ordering.copy(
                                            callYouId = menuItemUi.defaultValue
                                        )
                                    )
                                }
                                menuItemUi
                            } else menuItemUi
                        }
                    },
                    notifySection = notifySection,
                    totals = orderingDetails.totals.mapToUi(),
                    button = orderingDetails.button.toUi(),
                    selectedNotifyItem = s.selectedNotifyItem.takeIf { it ->
                        it != OrderNotifyItemUi.Empty
                    } ?: notifySection.items.firstOrNull() ?: s.selectedNotifyItem,
                    uiState = OrderingUiState.Order,
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

    fun navigateBack() = viewModelScope.launch {
        eventListener.emit(OrderingEvents.GoBack)
    }

    fun navigateBackWithRefresh() = viewModelScope.launch {
        eventListener.emit(OrderingEvents.RefreshCart)
        eventListener.emit(OrderingEvents.GoBack)
    }

    private fun changeOrderingSection(
        clearErrors: Boolean = true,
        section: SectionUi<OrderingMenuItemUi>,
        @Suppress("SameParameterValue")
        menuItemIds: List<String>,
        map: (OrderingMenuItemUi) -> OrderingMenuItemUi,
    ): SectionUi<OrderingMenuItemUi> {
        val updatedItems = section.items.map { item ->
            if (menuItemIds.contains(item.id)) map(item) else item.copy(error = if (clearErrors) false else item.error)
        }
        return section.copy(items = updatedItems)
    }

    private fun updateRecipientSection(
        ids: List<String>,
        map: (OrderingMenuItemUi) -> OrderingMenuItemUi,
    ) {
        uiStateListener.updateData { state ->
            state.copy(
                recipientSection = changeOrderingSection(
                    section = state.recipientSection,
                    menuItemIds = ids,
                    map = map
                ),
                paymentSection = changeOrderingSection(
                    section = state.paymentSection,
                    menuItemIds = emptyList(),
                    map = { it }
                )
            )
        }
    }

    fun navigateByRecipientItem(orderRecipientItem: OrderingMenuItemUi) = viewModelScope.launch {
        val ordering = stateSnapshot.ordering
        val addressId = ordering.addressId
        val timeInterval = ordering.timeInterval
        val date = ordering.date

        when (orderRecipientItem.id) {
            ADDRESS_MENU_ID -> {
                eventListener.emit(OrderingEvents.GoToAddresses(addressId))
            }

            RECIPIENT_MENU_ID -> {
                if (addressId == null) {
                    updateRecipientSection(listOf(ADDRESS_MENU_ID)) { item ->
                        item.copy(
                            error = true,
                            description = resourcesProvider.getString(
                                R.string.data_not_filled_error
                            )
                        )
                    }
                } else {
                    eventListener.emit(OrderingEvents.GoToOrderRecipient(addressId))
                }
            }

            DELIVERY_TIME_MENU_ID -> {
                if (addressId == null) {
                    updateRecipientSection(listOf(ADDRESS_MENU_ID)) { item ->
                        item.copy(
                            error = true,
                            description = resourcesProvider.getString(
                                R.string.data_not_filled_error
                            )
                        )
                    }
                } else {
                    eventListener.emit(
                        OrderingEvents.GoToDeliveryDate(
                            addressId,
                            date,
                            timeInterval
                        )
                    )
                }

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

    fun navigateByPaymentItem(orderPaymentItem: OrderingMenuItemUi) = viewModelScope.launch {
        val ordering = stateSnapshot.ordering
        val addressId = ordering.addressId
        val timeInterval = ordering.timeInterval
        val date = ordering.date
        val callYouId = ordering.callYouId
        val paymentMethodId = ordering.paymentId
        val balance = ordering.paymentBalance

        when (orderPaymentItem.id) {
            PAYMENT_MENU_ID -> {
                val recipientErrors = buildList {
                    if (addressId == null) {
                        add(ADDRESS_MENU_ID)
                    }
                    if (timeInterval == null || date == null) {
                        add(DELIVERY_TIME_MENU_ID)
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
                    eventListener.emit(OrderingEvents.ScrollToTop)
                    return@launch
                } else {
                    val localDate = LocalDate.parse(date, VodovozDateFormatters.DMY)
                    eventListener.emit(
                        OrderingEvents.GoToPaymentMethod(
                            addressId = addressId,
                            date = localDate,
                            paymentMethodId = paymentMethodId,
                            balance = balance
                        )
                    )
                }

            }

            else -> {
                if (addressId == null) {
                    updateRecipientSection(listOf(ADDRESS_MENU_ID)) { item ->
                        item.copy(
                            error = true,
                            description = resourcesProvider.getString(
                                R.string.data_not_filled_error
                            )
                        )
                    }
                    eventListener.emit(OrderingEvents.ScrollToTop)
                } else {
                    eventListener.emit(OrderingEvents.GoToCallYou(addressId, callYouId))
                }
            }
        }
    }

    fun doOrder(deviceInfo: String) = viewModelScope.launch {
        val ordering = stateSnapshot.ordering
        val earlierDelivery = ordering.earlierDelivery

        if (
            ordering.addressId != null && ordering.date != null
            && ordering.timeInterval != null && ordering.recipientPhone != null
            && ordering.paymentId != null && ordering.callYouId != null
        ) {
            uiStateListener.updateData { s ->
                s.copy(button = s.button.copy(loading = true))
            }

            vodovozServiceRepository.doOrder(
                addressId = ordering.addressId,
                deliveryDate = ordering.date,
                deliveryTimeInterval = ordering.timeInterval,
                phone = ordering.recipientPhone,
                paymentMethodId = ordering.paymentId.toLongOrNull() ?: 0,
                callYouId = ordering.callYouId.toLongOrNull(),
                coupon = coupon,
                balance = VodovozBoolean.from(ordering.paymentBalance).value,
                deviceInfo = deviceInfo,
                notifyDriverId = stateSnapshot.selectedNotifyItem.value,
                params = earlierDelivery?.let { mapOf(earlierDelivery) }
            ).singleResult().onSuccess { placeholder ->
                uiStateListener.updateData { s ->
                    s.copy(uiState = OrderingUiState.Success(placeholder.toUi()))
                }
            }

            uiStateListener.updateData { s ->
                s.copy(button = s.button.copy(loading = false))
            }

        } else {
            validateOrderingDetails()
        }
    }

    private fun validateOrderingDetails() = viewModelScope.launch {
        val ordering = stateSnapshot.ordering
        val recipientErrors = buildList {
            if (ordering.addressId == null) add(ADDRESS_MENU_ID)
            if (ordering.recipientName == null || ordering.recipientPhone == null) add(
                RECIPIENT_MENU_ID
            )
            if (ordering.timeInterval == null || ordering.date == null) add(DELIVERY_TIME_MENU_ID)
        }

        val paymentErrors = buildList {
            if (ordering.paymentId == null) add(PAYMENT_MENU_ID)
            if (ordering.callYouId == null) add(CALL_YOU_MENU_ID)
        }

        uiStateListener.updateData { s ->
            s.copy(
                recipientSection = changeOrderingSection(
                    section = s.recipientSection,
                    menuItemIds = recipientErrors,
                    map = { menuItem ->
                        menuItem.copy(
                            error = true,
                            description = resourcesProvider.getString(R.string.data_not_filled_error)
                        )
                    }
                ),
                paymentSection = changeOrderingSection(
                    section = s.paymentSection,
                    menuItemIds = paymentErrors,
                    map = { menuItem ->
                        menuItem.copy(
                            error = true,
                            description = resourcesProvider.getString(R.string.data_not_filled_error)
                        )
                    }
                )
            )
        }

        if (recipientErrors.isNotEmpty()) {
            eventListener.emit(OrderingEvents.ScrollToTop)
            return@launch
        }
    }

    fun refreshRecipient() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(showRefreshIndicator = true)
        }

        fetchRecipient()

        delay(350)

        uiStateListener.updateData { s ->
            s.copy(showRefreshIndicator = false)
        }

    }

    private fun fetchRecipient() = viewModelScope.launch {
        val addressId = stateSnapshot.ordering.addressId ?: return@launch
        val recipientResult = vodovozServiceRepository.getRecipient(addressId).singleResult()

        recipientResult.onSuccess { recipientModel ->

            uiStateListener.updateData { s ->
                val updatedOrdering = s.ordering.copy(
                    recipientPhone = recipientModel.phone.takeIf { it.isNotBlank() },
                    recipientName = recipientModel.fio.takeIf { it.isNotBlank() }
                )

                val orderingPhone = updatedOrdering.recipientPhone
                val orderingName = updatedOrdering.recipientName

                s.copy(
                    ordering = updatedOrdering,
                    recipientSection = if (orderingPhone != null && orderingName != null) {
                        changeOrderingSection(
                            clearErrors = false,
                            section = s.recipientSection,
                            menuItemIds = listOf(RECIPIENT_MENU_ID),
                            map = { menuItemUi ->
                                menuItemUi.copy(
                                    name = orderingPhone,
                                    description = orderingName,
                                    error = false
                                )
                            }
                        )
                    } else s.recipientSection
                )
            }
        }
    }

    fun setAddress(address: AddressUi) = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(
                ordering = OrderingUi.Empty.copy(addressId = address.id),
                recipientSection = changeOrderingSection(
                    clearErrors = false,
                    section = s.recipientSection,
                    menuItemIds = listOf(ADDRESS_MENU_ID),
                    map = { itemUi ->
                        itemUi.copy(
                            error = false,
                            name = address.address,
                            description = address.description
                        )
                    }
                ),
                showRefreshIndicator = true
            )
        }

        val orderingDetailsResult = vodovozServiceRepository.getOrderingDetails().singleResult()

        orderingDetailsResult.onSuccess { orderingDetails ->
            uiStateListener.updateData { s ->
                val recipientSection = s.recipientSection

                s.copy(
                    totals = orderingDetails.totals.mapToUi(),
                    paymentSection = orderingDetails.paymentSection.toUi {
                        it.mapToUi()
                    },
                    recipientSection = orderingDetails.recipientSection.toUi { list ->
                        list.mapToUi().map { menuItemUi ->
                            if (menuItemUi.id == ADDRESS_MENU_ID) {
                                recipientSection.items.find { recipientItem ->
                                    recipientItem.id == ADDRESS_MENU_ID
                                } ?: menuItemUi
                            } else {
                                menuItemUi
                            }
                        }
                    }
                )
            }
        }

        refreshRecipient()
    }

    fun setDeliveryDateTime(timeInterval: DeliveryTimeIntervalUi, date: DeliveryDateOptionUi) =
        viewModelScope.launch {
            uiStateListener.updateData { s ->
                s.copy(
                    ordering = s.ordering.copy(
                        timeInterval = timeInterval.value,
                        date = date.value,
                        paymentBalance = false,
                        paymentChange = null,
                        paymentId = null
                    ),
                    recipientSection = changeOrderingSection(
                        clearErrors = false,
                        section = s.recipientSection,
                        menuItemIds = listOf(DELIVERY_TIME_MENU_ID),
                        map = { itemUi ->
                            itemUi.copy(
                                error = false,
                                name = resourcesProvider.getString(
                                    R.string.date_time_inteval,
                                    date.value,
                                    timeInterval.name
                                ),
                                description = resourcesProvider.getString(R.string.delivery)
                            )
                        }
                    ),
                    showRefreshIndicator = true
                )
            }

            val ordering = stateSnapshot.ordering

            vodovozServiceRepository.getOrderingDetails(
                addressId = ordering.addressId,
                date = ordering.date,
                timeInterval = ordering.timeInterval
            ).singleResult().onSuccess { orderingDetails ->
                uiStateListener.updateData { s ->
                    s.copy(
                        paymentSection = s.paymentSection.copy(
                            items = s.paymentSection.items.map { menuItem ->
                                if (menuItem.id == PAYMENT_MENU_ID) {
                                    orderingDetails.paymentSection.items.mapToUi()
                                        .find { it.id == PAYMENT_MENU_ID } ?: menuItem
                                } else menuItem
                            }
                        ),
                        totals = orderingDetails.totals.mapToUi()
                    )
                }
            }

            uiStateListener.updateData { s ->
                s.copy(showRefreshIndicator = false)
            }
        }

    fun setCallYou(callYouItem: CallYouItemUi) = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(
                ordering = s.ordering.copy(
                    callYouId = callYouItem.value
                ),
                paymentSection = changeOrderingSection(
                    clearErrors = false,
                    section = s.paymentSection,
                    menuItemIds = listOf(CALL_YOU_MENU_ID),
                    map = { itemUi ->
                        itemUi.copy(
                            error = false,
                            description = callYouItem.name
                        )
                    }
                )
            )
        }
    }

    fun setPaymentBalance(paymentBalance: PaymentMethodItemUi) {
        uiStateListener.updateData { s ->
            s.copy(
                ordering = s.ordering.copy(paymentBalance = paymentBalance.value)
            )
        }
    }

    fun setPaymentMethod(paymentMethod: PaymentMethodItemUi) {
        uiStateListener.updateData { s ->
            s.copy(
                ordering = s.ordering.copy(
                    paymentId = paymentMethod.id,
                    paymentChange = paymentMethod.field?.value?.filter { char ->
                        char.isDigit()
                    }?.toIntRoundOrNull()?.toString()
                ),
                paymentSection = changeOrderingSection(
                    clearErrors = false,
                    section = s.paymentSection,
                    menuItemIds = listOf(PAYMENT_MENU_ID),
                    map = { orderingMenu ->
                        orderingMenu.copy(
                            error = false,
                            image = paymentMethod.image,
                            name = paymentMethod.name,
                            description = resourcesProvider.getString(
                                R.string.payment_method
                            )
                        )
                    }
                )
            )
        }
    }

    fun activatePayButton(button: ColorfulButtonUi?) = viewModelScope.launch {
        if (button?.url == null) {
            navigateBackWithRefresh()
            return@launch
        }

        eventListener.emit(OrderingEvents.RefreshCart)

        if (button.browser == true) {
            eventListener.emit(OrderingEvents.GoToWebView(button.url))
        } else {
            eventListener.emit(OrderingEvents.OpenUrl(button.url))
        }
    }

    fun setEarlierDelivery(checkbox: CheckboxUi) = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(
                ordering = s.ordering.copy(earlierDelivery = checkbox.id to checkbox.value())
            )
        }
    }

    @Immutable
    data class OrderingState(
        val title: String = "",
        val comment: FieldUi? = null,
        val paymentSection: SectionUi<OrderingMenuItemUi> = SectionUi.empty(),
        val notifySection: SectionUi<OrderNotifyItemUi> = SectionUi.empty(),
        val selectedNotifyItem: OrderNotifyItemUi = OrderNotifyItemUi.Empty,
        val recipientSection: SectionUi<OrderingMenuItemUi> = SectionUi.empty(),
        val totals: List<OrderSummaryItemUi> = emptyList(),
        val button: ColorfulButtonUi = ColorfulButtonUi.Empty,
        val uiState: OrderingUiState = OrderingUiState.Loading,
        val showRefreshIndicator: Boolean = false,
        val ordering: OrderingUi = OrderingUi.Empty,
    ) : State

    sealed class OrderingEvents : Event {
        data object GoBack : OrderingEvents()
        data class GoToAddresses(val addressId: Long?) : OrderingEvents()
        data object ScrollToTop : OrderingEvents()
        data object RefreshCart : OrderingEvents()

        data class GoToDeliveryDate(
            val addressId: Long,
            val date: String?,
            val timeInterval: String?,
        ) : OrderingEvents()

        data class GoToPaymentMethod(
            val addressId: Long,
            val date: LocalDate,
            val paymentMethodId: String?,
            val balance: Boolean?,
        ) : OrderingEvents()

        data class GoToOrderRecipient(val addressId: Long) : OrderingEvents()
        data class GoToCallYou(val addressId: Long, val callYouId: String?) : OrderingEvents()
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