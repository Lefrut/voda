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
import com.m.vodovoz.core.navigation.navigateToHome
import com.m.vodovoz.core.navigation.navigateToOrderCallYou
import com.m.vodovoz.core.navigation.navigateToOrderRecipient
import com.m.vodovoz.core.navigation.navigateToPaymentMethod
import com.m.vodovoz.core.navigation.navigateToWebView
import com.m.vodovoz.design_system.composables.placeholders.VodovozLongPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.addresses.model.AddressScreenTypeUi
import com.m.vodovoz.feature.cart.ordering.api.OrderingNavKey
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.util.extensions.openUrl

@Composable
fun OrderingEntry(
    onRefreshCart: () -> Unit,
    navKey: OrderingNavKey,
) = NavigationEntry<OrderingFlowViewModel, OrderingFlowViewModel.Factory>(
    creationCallback = { factory -> factory.create(navKey) }
) {
    val viewState by viewModel.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    LifecycleStartEffect(Unit) {
        viewModel.tabManager.setTabVisibility(false)
        onStopOrDispose {
            viewModel.tabManager.setTabVisibility(true)
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
                viewModel.tabManager.setTabVisibility(true)
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
    viewModel.events.collect { event ->
        when (event) {
            OrderingFlowViewModel.OrderingEvents.GoBack -> {
                navigator.goBack()
            }

            is OrderingFlowViewModel.OrderingEvents.GoToAddresses -> {
                navigator.navigateToAddresses(
                    AddressScreenTypeUi.Choose,
                    event.addressId
                )
            }

            is OrderingFlowViewModel.OrderingEvents.GoToDeliveryDate -> {
                navigator.navigateToDeliveryDate(
                    earlierDelivery = event.earlierDelivery,
                    addressId = event.addressId,
                    date = event.date,
                    timeInterval = event.timeInterval,
                    queryParams = event.queryParams
                )
            }

            is OrderingFlowViewModel.OrderingEvents.GoToPaymentMethod -> {
                navigator.navigateToPaymentMethod(
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
                navigator.navigateToOrderRecipient(event.addressId)
            }

            is OrderingFlowViewModel.OrderingEvents.GoToCallYou -> {
                navigator.navigateToOrderCallYou(
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
                navigator.navigateToWebView(
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
                navigator.goBack()
            }

            OrderingFlowViewModel.OrderingEvents.GoToHome -> {
                navigator.navigateToHome()
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
