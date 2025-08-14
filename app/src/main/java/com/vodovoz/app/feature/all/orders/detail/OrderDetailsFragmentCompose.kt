package com.vodovoz.app.feature.all.orders.detail

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.R
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.core.navigation.mainFragment
import com.vodovoz.app.core.navigation.navigateToCancelOrder
import com.vodovoz.app.core.navigation.navigateToOrderQuestion
import com.vodovoz.app.core.navigation.navigateToProductDetails
import com.vodovoz.app.core.navigation.navigateToTraceOrder
import com.vodovoz.app.core.navigation.navigateToWebView
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.composables.placeholders.LoadingPlaceholder
import com.vodovoz.app.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.vodovoz.app.design_system.composables.snackbar.VodovozSnackBarVisuals
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.feature.all.orders.detail.composables.AboutOrderBottomSheet
import com.vodovoz.app.ui.snackbar.snackBarHostState
import com.vodovoz.app.util.extensions.copyText
import com.vodovoz.app.util.extensions.openUrl
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
                    val pagingState by viewModel.state.collectAsStateWithLifecycle()
                    val viewState by rememberUpdatedState(newValue = pagingState)

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
                        viewModel.listenFavorites()
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
