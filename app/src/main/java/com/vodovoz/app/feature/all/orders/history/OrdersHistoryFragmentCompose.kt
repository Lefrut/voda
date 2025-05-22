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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vodovoz.app.R
import com.vodovoz.app.common.account.data.AccountManager
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.core.navigation.navigateToOrderDetails
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.effects.LifecycleEffect
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OrdersHistoryFragment : Fragment() {

    internal val viewModel: AllOrdersFlowViewModel by viewModels()

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
                    val pagingState by viewModel.observeUiState().collectAsStateWithLifecycle()
                    val viewState by rememberUpdatedState(newValue = pagingState.data)

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
        viewModel.observeEvent().collect { event ->
            when (event) {
                is AllOrdersFlowViewModel.AllOrdersEvent.GoToCart -> {
                    //TODO
                }

                AllOrdersFlowViewModel.AllOrdersEvent.GoBack -> {
                    findNavController().popBackStack()
                }

                AllOrdersFlowViewModel.AllOrdersEvent.GoToCatalog -> {
                    findNavController().popBackStack()
                    tabManager.selectTab(R.id.graph_catalog)
                }

                is AllOrdersFlowViewModel.AllOrdersEvent.GoToOrderDetails -> {
                    findNavController().navigateToOrderDetails(event.id)
                }
            }
        }
    }

}
