package com.m.vodovoz.feature.all.orders.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.m.vodovoz.R
import com.m.vodovoz.common.account.AccountManager
import com.m.vodovoz.common.cookie.CookieManager
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.core.navigation.activate
import com.m.vodovoz.core.navigation.navigateToOrderDetails
import com.m.vodovoz.core.navigation.navigateToProductAnalogs
import com.m.vodovoz.core.navigation.navigateToProductDetails
import com.m.vodovoz.core.navigation.navigateToWebView
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.util.extensions.openUrl
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filter
import javax.inject.Inject

@AndroidEntryPoint
class OrdersHistoryFragment : Fragment() {

    internal val viewModel: OrdersHistoryViewModel by viewModels()

    @Inject
    lateinit var accountManager: AccountManager

    @Inject
    lateinit var tabManager: TabManager

    @Inject
    lateinit var cookieManager: CookieManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                VodovozTheme {
                    val viewState by viewModel.collectAsState()


                    OrdersHistoryScreen(
                        viewModel = viewModel,
                        viewState = viewState
                    )

                    LifecycleEffect {
                        observeEvents()
                    }

                    LifecycleEffect {
                        observeAccount()
                    }
                }
            }
        }
    }

    private suspend fun observeAccount() {
        accountManager.observeAccountId().filter { it == null }.collect {
            val navController = findNavController()
            navController.popBackStack()
            tabManager.selectTab(R.id.graph_profile)
        }
    }

    private suspend fun observeEvents() {
        viewModel.events.collect { event ->
            val navController = findNavController()
            val context = requireContext()

            when (event) {
                is OrdersHistoryViewModel.AllOrdersEvent.GoToCart -> {
                    tabManager.selectTab(R.id.graph_cart)
                }

                OrdersHistoryViewModel.AllOrdersEvent.GoBack -> {
                    findNavController().popBackStack()
                }

                OrdersHistoryViewModel.AllOrdersEvent.GoToCatalog -> {
                    findNavController().popBackStack()
                    tabManager.selectTab(R.id.graph_catalog)
                }

                is OrdersHistoryViewModel.AllOrdersEvent.GoToOrderDetails -> {
                    findNavController().navigateToOrderDetails(event.id)
                }

                is OrdersHistoryViewModel.AllOrdersEvent.GoToProductDetails -> {
                    findNavController().navigateToProductDetails(event.id)
                }

                is OrdersHistoryViewModel.AllOrdersEvent.GoToProductAnalogs -> {
                    findNavController().navigateToProductAnalogs(event.id)
                }

                is OrdersHistoryViewModel.AllOrdersEvent.GoToWebView -> {
                    val space = requireContext().getString(R.string.space)
                    findNavController().navigateToWebView(
                        url = event.url,
                        title = space
                    )
                }

                is OrdersHistoryViewModel.AllOrdersEvent.OpenUrl -> {
                    context.openUrl(event.url)
                }

                is OrdersHistoryViewModel.AllOrdersEvent.ActivateBanner -> {
                    event.banner.action.activate(
                        navController = navController,
                        context = context,
                        cookie = cookieManager.fetchCookieSessionId() ?: "",
                        tabManager = tabManager
                    )
                }
            }
        }
    }

}
