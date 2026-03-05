package com.m.vodovoz.feature.all.orders.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleStartEffect
import com.m.vodovoz.R
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.core.navigation.navigateToCancelOrder
import com.m.vodovoz.core.navigation.navigateToOrderQuestion
import com.m.vodovoz.core.navigation.navigateToProductDetails
import com.m.vodovoz.core.navigation.navigateToTraceOrder
import com.m.vodovoz.core.navigation.navigateToWebView
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.all.orders.detail.composables.AboutOrderBottomSheet
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.util.extensions.copyText
import com.m.vodovoz.util.extensions.openUrl
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter

@Composable
fun OrderDetailsEntry(
    onOrderIdCopied: () -> Unit,
) = NavigationEntry<OrderDetailsFlowViewModel> {
    val viewState by viewModel.collectAsState()
    val context = LocalContext.current

    LifecycleStartEffect(Unit) {
        viewModel.fetchOrderDetails()
        onStopOrDispose { }
    }

    when (viewState.uiState) {
        OrderDetailsFlowViewModel.OrderDetailsUiState.Body -> {
            OrderDetailsScreen(
                viewModel = viewModel,
                viewState = viewState
            )
        }

        OrderDetailsFlowViewModel.OrderDetailsUiState.Error -> {
            NetworkErrorPlaceholder {
                viewModel.fetchOrderDetails()
            }
        }

        OrderDetailsFlowViewModel.OrderDetailsUiState.Loading -> {
            LoadingPlaceholder()
        }
    }

    val currentAboutOrder = viewState.currentAboutOrderBS
    if (viewState.showAboutOrderBS && currentAboutOrder != null) {
        AboutOrderBottomSheet(data = currentAboutOrder) {
            viewModel.closeAboutOrderBottomSheet()
        }
    }

    LifecycleEffect {
        viewModel.accountManager.observeAccountId().filter { it == null }.collect {
            navController.popBackStack()
            viewModel.tabManager.selectTab(R.id.graph_profile)
        }
    }

    LifecycleEffect {
        viewModel.events.collectLatest { event ->
            when (event) {
                is OrderDetailsFlowViewModel.OrderDetailsEvent.CopyText -> {
                    context.copyText(event.text)
                    onOrderIdCopied()
                }

                OrderDetailsFlowViewModel.OrderDetailsEvent.GoBack -> {
                    navController.popBackStack()
                }

                is OrderDetailsFlowViewModel.OrderDetailsEvent.GoToOrderQuestion -> {
                    navController.navigateToOrderQuestion(event.orderId)
                }

                is OrderDetailsFlowViewModel.OrderDetailsEvent.GoToCancelOrder -> {
                    navController.navigateToCancelOrder(event.orderId)
                }

                is OrderDetailsFlowViewModel.OrderDetailsEvent.GoToProductDetails -> {
                    navController.navigateToProductDetails(event.productId)
                }

                is OrderDetailsFlowViewModel.OrderDetailsEvent.GoToTraceOrder -> {
                    navController.navigateToTraceOrder(
                        event.dividerId,
                        event.orderId
                    )
                }

                is OrderDetailsFlowViewModel.OrderDetailsEvent.GoToWebView -> {
                    val space = context.getString(R.string.space)
                    navController.navigateToWebView(
                        event.url,
                        space
                    )
                }

                is OrderDetailsFlowViewModel.OrderDetailsEvent.OpenUrl -> {
                    context.openUrl(event.url)
                }

                OrderDetailsFlowViewModel.OrderDetailsEvent.GoToCart -> {
                    viewModel.tabManager.selectTab(R.id.graph_cart)
                }
            }
        }
    }
}
