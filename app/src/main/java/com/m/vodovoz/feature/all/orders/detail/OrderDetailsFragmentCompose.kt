package com.m.vodovoz.feature.all.orders.detail

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.m.vodovoz.R
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.core.navigation.mainFragment
import com.m.vodovoz.core.navigation.navigateToCancelOrder
import com.m.vodovoz.core.navigation.navigateToOrderQuestion
import com.m.vodovoz.core.navigation.navigateToProductDetails
import com.m.vodovoz.core.navigation.navigateToTraceOrder
import com.m.vodovoz.core.navigation.navigateToWebView
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.composables.snackbar.VodovozSnackBarVisuals
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.all.orders.detail.composables.AboutOrderBottomSheet
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.ui.snackbar.snackBarHostState
import com.m.vodovoz.util.extensions.copyText
import com.m.vodovoz.util.extensions.openUrl
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class OrderDetailsFragment : Fragment() {

    internal val viewModel: OrderDetailsFlowViewModel by viewModels()

    @Inject
    lateinit var tabManager: TabManager

    override fun onResume() {
        super.onResume()
        viewModel.fetchOrderDetails()
    }

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                VodovozTheme {
                    val viewState by viewModel.collectAsState()
                    

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
                        viewModel.events.collectLatest { event ->
                            when (event) {
                                is OrderDetailsFlowViewModel.OrderDetailsEvent.CopyText -> {
                                    requireContext().copyText(event.text)
                                    mainFragment?.snackBarHostState?.showSnackbar(
                                        VodovozSnackBarVisuals.create(getString(R.string.order_number_copied))
                                    )
                                }

                                OrderDetailsFlowViewModel.OrderDetailsEvent.GoBack -> {
                                    findNavController().popBackStack()
                                }

                                is OrderDetailsFlowViewModel.OrderDetailsEvent.GoToOrderQuestion -> {
                                    findNavController().navigateToOrderQuestion(event.orderId)
                                }

                                is OrderDetailsFlowViewModel.OrderDetailsEvent.GoToCancelOrder -> {
                                    findNavController().navigateToCancelOrder(event.orderId)
                                }

                                is OrderDetailsFlowViewModel.OrderDetailsEvent.GoToProductDetails -> {
                                    findNavController().navigateToProductDetails(event.productId)
                                }

                                is OrderDetailsFlowViewModel.OrderDetailsEvent.GoToTraceOrder -> {
                                    findNavController().navigateToTraceOrder(
                                        event.dividerId,
                                        event.orderId
                                    )
                                }

                                is OrderDetailsFlowViewModel.OrderDetailsEvent.GoToWebView -> {
                                    val space = requireContext().getString(R.string.space)
                                    findNavController().navigateToWebView(
                                        event.url, space
                                    )
                                }

                                is OrderDetailsFlowViewModel.OrderDetailsEvent.OpenUrl -> {
                                    requireContext().openUrl(event.url)
                                }

                                OrderDetailsFlowViewModel.OrderDetailsEvent.GoToCart -> {
                                    tabManager.selectTab(R.id.graph_cart)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
