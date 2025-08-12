package com.vodovoz.app.feature.all.orders.detail

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.common.cart.CartManager
import com.vodovoz.app.common.content.Event
import com.vodovoz.app.common.content.PagingContractViewModel
import com.vodovoz.app.common.content.State
import com.vodovoz.app.common.content.updateData
import com.vodovoz.app.common.like.LikeManager
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.OrderProductUi
import com.vodovoz.app.design_system.model.mapToUi
import com.vodovoz.app.design_system.model.order.OrderSummaryItemUi
import com.vodovoz.app.design_system.model.order.mapToUi
import com.vodovoz.app.design_system.model.withUpdatedFavorites
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.all.orders.detail.composables.AboutOrderPopupWindowUi
import com.vodovoz.app.feature.all.orders.detail.model.OrderDetailsButtonUi
import com.vodovoz.app.feature.all.orders.detail.model.OrderStatusUi
import com.vodovoz.app.feature.all.orders.detail.model.mapToUi
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class OrderDetailsFlowViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val cartManager: CartManager,
    private val likeManager: LikeManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
) : PagingContractViewModel<OrderDetailsFlowViewModel.OrderDetailsState, OrderDetailsFlowViewModel.OrderDetailsEvent>(
    OrderDetailsState()
) {

    companion object {
        const val QUESTION_BUTTON_ID = "voproszakaz"
    }

    private val orderId = savedState.get<Long>("orderId") ?: navigateBack().run { -1 }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun listenFavorites() =
        uiStateListener.map { it.data.products }.combine(likeManager.observeLikes()) { _, p2 ->
            p2
        }.mapLatest { favorites ->
            uiStateListener.updateData { s ->
                s.copy(products = s.products.withUpdatedFavorites(favorites))
            }
        }.collect()


    fun navigateBack() = viewModelScope.launch {
        eventListener.emit(OrderDetailsEvent.GoBack)
    }

    fun copyOrderId() = viewModelScope.launch {
        eventListener.emit(OrderDetailsEvent.CopyText(orderId.toString()))
    }

    fun fetchOrderDetails() = viewModelScope.launch {
        if (stateSnapshot.uiState !is OrderDetailsUiState.Body) {
            uiStateListener.updateData { s ->
                s.copy(uiState = OrderDetailsUiState.Loading)
            }
        }

        val orderDetailsResult =
            vodovozServiceRepository.getOrderDetails(orderId).singleResult()

        orderDetailsResult.onSuccess { orderDetails ->

            val bottomButtons = orderDetails.bottomButtons.mapToUi()
            val questionButton = bottomButtons.find { it.id == QUESTION_BUTTON_ID }

            uiStateListener.updateData { s ->
                s.copy(
                    topButtons = orderDetails.topButtons.mapToUi(),
                    bottomButtons = bottomButtons,
                    questionButton = questionButton,
                    title = orderDetails.title,
                    subtitle = orderDetails.subtitle,
                    header = orderDetails.header,
                    orderSummary = orderDetails.orderSummary.mapToUi(),
                    statuses = orderDetails.statuses.mapToUi(),
                    currentStatuses = orderDetails.currentStatus.mapToUi(),
                    productsTitle = orderDetails.productsTitle,
                    products = orderDetails.products.mapToUi(),
                    uiState = OrderDetailsUiState.Body
                )
            }

        }.onFailure {
            uiStateListener.updateData { s ->
                s.copy(uiState = OrderDetailsUiState.Error)
            }
        }
    }

    fun activateTopButton(orderDetailsButton: OrderDetailsButtonUi) = viewModelScope.launch {
        when {
            orderDetailsButton.popupWindow != null -> {
                showAboutOrderBottomSheet(orderDetailsButton.popupWindow ?: return@launch)
            }

            orderDetailsButton.id == "voditel" -> {
                eventListener.emit(
                    OrderDetailsEvent.GoToTraceOrder(
                        orderDetailsButton.driverId ?: return@launch,
                        orderId
                    )
                )
            }

            orderDetailsButton.id == "oplata" || orderDetailsButton.url != null -> {
                val url = orderDetailsButton.url ?: return@launch
                val event = if (orderDetailsButton.browser == true) {
                    OrderDetailsEvent.OpenUrl(url)
                } else {
                    OrderDetailsEvent.GoToWebView(url)
                }
                eventListener.emit(event)
            }

            orderDetailsButton.id == "povtorit" -> {
                vodovozServiceRepository.repeatOrder(orderId).singleResult()
                    .onSuccess {
                        cartManager.updateCartListState(true)
                        eventListener.emit(OrderDetailsEvent.GoToCart)
                    }
            }
        }
    }


    fun activateBottomButton(button: ColorfulButtonUi) = viewModelScope.launch {
        when (button.id) {
            QUESTION_BUTTON_ID -> {
                eventListener.emit(OrderDetailsEvent.GoToOrderQuestion(orderId))
            }

            "otmena" -> {
                eventListener.emit(OrderDetailsEvent.GoToCancelOrder(orderId))
            }
        }
    }

    fun changeProductFavorite(orderProduct: OrderProductUi) = viewModelScope.launch {
        likeManager.changeFavorite(orderProduct.id, !orderProduct.isFavorite)
    }

    fun navigateToProductDetails(orderProduct: OrderProductUi) = viewModelScope.launch {
        eventListener.emit(OrderDetailsEvent.GoToProductDetails(orderProduct.id))
    }

    private fun showAboutOrderBottomSheet(aboutOrderBottomSheet: AboutOrderPopupWindowUi) =
        viewModelScope.launch {
            uiStateListener.updateData { s ->
                s.copy(
                    showAboutOrderBS = true,
                    currentAboutOrderBS = aboutOrderBottomSheet
                )
            }
        }

    fun closeAboutOrderBottomSheet() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(showAboutOrderBS = false)
        }
    }

    fun refresh() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(showRefreshIndicator = true)
        }

        fetchOrderDetails().join()

        uiStateListener.updateData { s ->
            s.copy(showRefreshIndicator = false)
        }

    }

    @Immutable
    data class OrderDetailsState(
        val title: String = "",
        val subtitle: String = "",
        val header: String = "",
        val topButtons: List<OrderDetailsButtonUi> = emptyList(),
        val bottomButtons: List<ColorfulButtonUi> = emptyList(),
        val questionButton: ColorfulButtonUi? = null,
        val orderSummary: List<OrderSummaryItemUi> = emptyList(),
        val currentStatuses: List<OrderStatusUi> = emptyList(),
        val statuses: List<OrderStatusUi> = emptyList(),
        val productsTitle: String = "",
        val products: List<OrderProductUi> = emptyList(),
        val currentAboutOrderBS: AboutOrderPopupWindowUi? = null,
        val showAboutOrderBS: Boolean = false,
        val uiState: OrderDetailsUiState = OrderDetailsUiState.Loading,
        val showRefreshIndicator: Boolean = false,
    ) : State

    sealed class OrderDetailsEvent : Event {
        data object GoBack : OrderDetailsEvent()
        data object GoToCart : OrderDetailsEvent()
        data class CopyText(val text: String) : OrderDetailsEvent()
        data class GoToOrderQuestion(val orderId: Long) : OrderDetailsEvent()
        data class GoToCancelOrder(val orderId: Long) : OrderDetailsEvent()
        data class GoToProductDetails(val productId: Long) : OrderDetailsEvent()
        data class GoToTraceOrder(val dividerId: String, val orderId: Long) : OrderDetailsEvent()
        data class GoToWebView(val url: String) : OrderDetailsEvent()
        data class OpenUrl(val url: String) : OrderDetailsEvent()
    }

    @Stable
    sealed interface OrderDetailsUiState {
        data object Loading : OrderDetailsUiState
        data object Error : OrderDetailsUiState
        data object Body : OrderDetailsUiState
    }
}