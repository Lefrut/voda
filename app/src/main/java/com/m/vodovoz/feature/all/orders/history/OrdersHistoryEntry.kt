package com.m.vodovoz.feature.all.orders.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.m.vodovoz.R
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.core.navigation.activate
import com.m.vodovoz.core.navigation.navigateToOrderDetails
import com.m.vodovoz.core.navigation.navigateToWebView
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.util.extensions.openUrl
import kotlinx.coroutines.flow.filter

@Composable
fun OrdersHistoryEntry() = NavigationEntry<OrdersHistoryViewModel> {
    val viewState by viewModel.collectAsState()
    val context = LocalContext.current

    OrdersHistoryScreen(
        viewModel = viewModel,
        viewState = viewState
    )

    LifecycleEffect {
        viewModel.events.collect { event ->
            when (event) {
                is OrdersHistoryViewModel.AllOrdersEvent.GoToCart -> {
                    viewModel.tabManager.selectTab(R.id.graph_cart)
                }

                OrdersHistoryViewModel.AllOrdersEvent.GoBack -> {
                    navController.popBackStack()
                }

                OrdersHistoryViewModel.AllOrdersEvent.GoToCatalog -> {
                    navController.popBackStack()
                    viewModel.tabManager.selectTab(R.id.graph_catalog)
                }

                is OrdersHistoryViewModel.AllOrdersEvent.GoToOrderDetails -> {
                    navController.navigateToOrderDetails(event.id)
                }

                is OrdersHistoryViewModel.AllOrdersEvent.GoToWebView -> {
                    navController.navigateToWebView(
                        url = event.url,
                        title = context.getString(R.string.space)
                    )
                }

                is OrdersHistoryViewModel.AllOrdersEvent.OpenUrl -> {
                    context.openUrl(event.url)
                }

                is OrdersHistoryViewModel.AllOrdersEvent.ActivateBanner -> {
                    event.banner.action.activate(
                        navController = navController,
                        context = context,
                        cookie = viewModel.cookieManager.fetchCookieSessionId() ?: "",
                        tabManager = viewModel.tabManager
                    )
                }
            }
        }
    }

    LifecycleEffect {
        viewModel.accountManager.observeAccountId().filter { it == null }.collect {
            navController.popBackStack()
            viewModel.tabManager.selectTab(R.id.graph_profile)
        }
    }
}
