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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vodovoz.app.R
import com.vodovoz.app.common.account.data.AccountManager
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.core.navigation.navigateToOrderDetails
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.feature.all.AllClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeAccount()
    }

    private fun observeAccount() = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            accountManager.observeAccountId().collect { userId ->
                if (userId == null) {
                    findNavController().popBackStack()
                    tabManager.setAuthRedirect(findNavController().graph.id)
                    tabManager.selectTab(R.id.graph_profile)
                }
            }
        }
    }

    private suspend fun observeEvents() {
        viewModel.observeEvent().collect { event ->
            when (event) {
                is AllOrdersFlowViewModel.AllOrdersEvent.GoToFilter -> {

                }

                is AllOrdersFlowViewModel.AllOrdersEvent.GoToCart -> {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Товары добавлены в корзину")
                        .setMessage("Перейти в корзину?")
                        .setPositiveButton("Да") { dialog, _ ->
                            dialog.dismiss()
                            if (findNavController().currentBackStackEntry?.destination?.id == R.id.allOrdersFragment) {
                                findNavController().navigate(
                                    OrdersHistoryFragmentDirections.actionToCartFragment()
                                )
                            }
                        }
                        .setNegativeButton("Нет") { dialog, _ -> dialog.dismiss() }
                        .show()
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


    private fun getAllClickListener(): AllClickListener {
        return object : AllClickListener {
            override fun onMoreDetailClick(orderId: Long, sendReport: Boolean) {
                if (sendReport) {
                    val eventParameters = "\"ZakazID\":\"$orderId\""
                    accountManager.reportEvent(
                        "Зашел в заказ, статус в пути",
                        eventParameters
                    )
                }
                findNavController().navigate(
                    OrdersHistoryFragmentDirections.actionToOrderDetailsFragment(
                        orderId
                    )
                )
            }

            override fun onRepeatOrderClick(orderId: Long) {
                viewModel.repeatOrder(orderId)
            }

            override fun onProductDetailPictureClick(productId: Long) {
                findNavController().navigate(
                    OrdersHistoryFragmentDirections.actionToProductDetailFragment(
                        productId
                    )
                )
            }
        }
    }
}
