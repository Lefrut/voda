package com.vodovoz.app.feature.all.orders.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.R
import com.vodovoz.app.common.account.AccountManager
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.core.navigation.navigateToOrderDetails
import com.vodovoz.app.core.navigation.navigateToWebView
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.util.extensions.openUrl
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OrdersHistoryFragment : Fragment() {

    internal val viewModel: OrdersHistoryViewModel by viewModels()

    @Inject
    lateinit var accountManager: AccountManager

    @Inject
    lateinit var tabManager: TabManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                VodovozTheme {
                    val pagingState by viewModel.state.collectAsStateWithLifecycle()
                    val viewState by rememberUpdatedState(newValue = pagingState)

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
        accountManager.observeAccountId().collect { userId ->
            if (userId == null) {
                findNavController().popBackStack()
                tabManager.setAuthRedirect(findNavController().graph.id)
                tabManager.selectTab(R.id.graph_profile)
            }
        }
    }

    private suspend fun observeEvents() {
        viewModel.events.collect { event ->
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

                is OrdersHistoryViewModel.AllOrdersEvent.GoToWebView -> {
                    val space = requireContext().getString(R.string.space)
                    findNavController().navigateToWebView(
                        url = event.url,
                        title = space
                    )
                }

                is OrdersHistoryViewModel.AllOrdersEvent.OpenUrl -> {
                    requireContext().openUrl(event.url)
                }
            }
        }
    }

}
