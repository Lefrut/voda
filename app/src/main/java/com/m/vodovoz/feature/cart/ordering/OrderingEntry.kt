package com.m.vodovoz.feature.cart.ordering

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.navigation.navOptions
import com.m.vodovoz.R
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.core.navigation.navigateToAddresses
import com.m.vodovoz.core.navigation.navigateToDeliveryDate
import com.m.vodovoz.core.navigation.navigateToOrderCallYou
import com.m.vodovoz.core.navigation.navigateToOrderRecipient
import com.m.vodovoz.core.navigation.navigateToPaymentMethod
import com.m.vodovoz.core.navigation.navigateToWebView
import com.m.vodovoz.design_system.composables.placeholders.VodovozLongPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.design_system.model.widgets.CheckboxUi
import com.m.vodovoz.feature.addresses.model.AddressScreenTypeUi
import com.m.vodovoz.feature.addresses.model.AddressUi
import com.m.vodovoz.feature.delivery_date.model.DeliveryDateOptionUi
import com.m.vodovoz.feature.delivery_date.model.DeliveryTimeIntervalUi
import com.m.vodovoz.feature.order_call_you.model.CallYouItemUi
import com.m.vodovoz.feature.payment_method.model.PaymentMethodItemNav
import com.m.vodovoz.feature.payment_method.model.toUi
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.util.extensions.openUrl
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch

@Composable
fun OrderingEntry(
    onRefreshCart: () -> Unit,
) = NavigationEntry<OrderingFlowViewModel> {
    val viewState by viewModel.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    LifecycleStartEffect(Unit) {
        viewModel.tabManager.changeTabVisibility(false)
        viewModel.accountManager.reportEvent("Зашел на экран оформления заказа")
        onStopOrDispose {
            viewModel.tabManager.changeTabVisibility(true)
        }
    }

    when (val uiState = viewState.uiState) {
        OrderingFlowViewModel.OrderingUiState.Error,
        OrderingFlowViewModel.OrderingUiState.Loading,
        OrderingFlowViewModel.OrderingUiState.Order,
            -> {
            OrderingScreen(
                viewModel = viewModel,
                viewState = viewState,
                scrollState = scrollState
            )
        }

        is OrderingFlowViewModel.OrderingUiState.Success -> {
            VodovozLongPlaceholder(
                data = uiState.placeholder,
                onCloseClick = {
                    viewModel.navigateToHomeWithRefresh()
                },
                onButtonClick = {
                    viewModel.activatePayButton(uiState.placeholder.button)
                }
            )

            LaunchedEffect(Unit) {
                viewModel.tabManager.changeTabVisibility(true)
            }
        }
    }

    LifecycleEffect {
        observeEvents(scrollState = scrollState, context = context, onRefreshCart = onRefreshCart)
    }
}

private suspend fun com.m.vodovoz.core.navigation.NavigationEntryScope<OrderingFlowViewModel>.observeEvents(
    scrollState: ScrollState,
    context: android.content.Context,
    onRefreshCart: () -> Unit,
) {
    viewModel.events.onSubscription {
        val backEntrySavedStateHandle = navController.currentBackStackEntry?.savedStateHandle

        backEntrySavedStateHandle?.apply {
            remove<AddressUi>("back_address")?.let { address ->
                viewModel.resetOrderIfEmptyAddress(address)
            }

            remove<AddressUi>("address")?.let { address ->
                viewModel.setAddress(address)
            }

            val timeInterval = remove<DeliveryTimeIntervalUi>("timeInterval")
            val date = remove<DeliveryDateOptionUi>("dateOption")

            if (timeInterval != null && date != null) {
                viewModel.setDeliveryDateTime(timeInterval, date)
            }

            remove<CheckboxUi>("earlierCheckbox")?.let { checkbox ->
                viewModel.setEarlierDelivery(checkbox)
            }

            remove<CallYouItemUi>("callYou")?.let { callYouItem ->
                viewModel.setCallYou(callYouItem)
            }

            viewModel.setPaymentInfo(
                paymentBalance = remove<PaymentMethodItemNav>("paymentBalance")?.toUi(),
                paymentBonuses = remove<PaymentMethodItemNav>("paymentBonuses")?.toUi(),
                paymentMethod = remove<PaymentMethodItemNav>("paymentMethod")?.toUi()
            )

            remove<Boolean>("updateRecipient")?.let {
                viewModel.refreshRecipient()
            }
        }
    }.collect { event ->
        when (event) {
            OrderingFlowViewModel.OrderingEvents.GoBack -> {
                navController.popBackStack()
            }

            is OrderingFlowViewModel.OrderingEvents.GoToAddresses -> {
                navController.navigateToAddresses(
                    AddressScreenTypeUi.Choose,
                    event.addressId
                )
            }

            is OrderingFlowViewModel.OrderingEvents.GoToDeliveryDate -> {
                navController.navigateToDeliveryDate(
                    earlierDelivery = event.earlierDelivery,
                    addressId = event.addressId,
                    date = event.date,
                    timeInterval = event.timeInterval,
                    queryParams = event.queryParams
                )
            }

            is OrderingFlowViewModel.OrderingEvents.GoToPaymentMethod -> {
                navController.navigateToPaymentMethod(
                    addressId = event.addressId,
                    date = event.date,
                    paymentMethodId = event.paymentMethodId,
                    balance = event.balance,
                    bonuses = event.bonuses,
                    bonusesValue = event.bonusesValue,
                    paymentChange = event.paymentChange,
                    queryParams = event.queryParams
                )
            }

            is OrderingFlowViewModel.OrderingEvents.GoToOrderRecipient -> {
                navController.navigateToOrderRecipient(event.addressId)
            }

            is OrderingFlowViewModel.OrderingEvents.GoToCallYou -> {
                navController.navigateToOrderCallYou(
                    event.addressId,
                    event.callYouId,
                    event.queryParams
                )
            }

            OrderingFlowViewModel.OrderingEvents.ScrollToTop -> {
                scrollState.animateScrollTo(0)
            }

            OrderingFlowViewModel.OrderingEvents.RefreshCart -> {
                viewModel.tabManager.clearBottomNavCartState()
                onRefreshCart()
            }

            is OrderingFlowViewModel.OrderingEvents.GoToWebView -> {
                navController.navigateToWebView(
                    title = context.getString(R.string.space),
                    url = event.url,
                    navOptions = navOptions {
                        popUpTo(R.id.cartFragment) {
                            inclusive = false
                        }
                    }
                )
            }

            is OrderingFlowViewModel.OrderingEvents.OpenUrl -> {
                context.openUrl(event.url)
                navController.popBackStack()
            }

            OrderingFlowViewModel.OrderingEvents.GoToHome -> {
                navController.popBackStack()
                viewModel.tabManager.selectTab(R.id.graph_home)
            }

            OrderingFlowViewModel.OrderingEvents.ScrollToBottom -> {
                scrollState.animateScrollTo(scrollState.maxValue)
            }

            OrderingFlowViewModel.OrderingEvents.UpdateBottomCart -> {
                viewModel.tabManager.updateBottomNavCartState()
            }
        }
    }
}
