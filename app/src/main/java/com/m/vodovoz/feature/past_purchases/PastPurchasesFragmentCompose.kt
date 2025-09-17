package com.m.vodovoz.feature.past_purchases

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.m.vodovoz.R
import com.m.vodovoz.common.cart.CartManager
import com.m.vodovoz.common.like.LikeManager
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.core.navigation.navigateToProductAnalogs
import com.m.vodovoz.core.navigation.navigateToPreOrder
import com.m.vodovoz.core.navigation.navigateToProductDetails
import com.m.vodovoz.core.navigation.navigateToSearch
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.ui.mvi.collectAsState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PastPurchasesFragment : Fragment() {

    internal val viewModel: PastPurchasesFlowViewModel by viewModels()

    @Inject
    lateinit var cartManager: CartManager

    @Inject
    lateinit var likeManager: LikeManager

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
                    val viewState by viewModel.collectAsState()
                    val lazyGridState = rememberLazyGridState()

                    PastPurchasesScreen(
                        viewModel = viewModel,
                        viewState = viewState,
                        lazyGridState = lazyGridState
                    )


                    LifecycleEffect {
                        listenEvents()
                    }
                }
            }
        }
    }

    private suspend fun listenEvents(): Unit = viewModel.events.collect { event ->
        when (event) {
            is PastPurchasesFlowViewModel.PastPurchasesEvents.GoToPreOrder -> {
                findNavController().navigateToPreOrder(event.id)
            }

            is PastPurchasesFlowViewModel.PastPurchasesEvents.GoToProfile -> {
                findNavController().popBackStack(R.id.profileFragment, false)
                tabManager.setAuthRedirect(findNavController().graph.id)
                tabManager.selectTab(R.id.graph_profile)
            }

            PastPurchasesFlowViewModel.PastPurchasesEvents.GoBack -> {
                findNavController().popBackStack()
            }

            PastPurchasesFlowViewModel.PastPurchasesEvents.GoToSearch -> {
                findNavController().navigateToSearch()
            }

            is PastPurchasesFlowViewModel.PastPurchasesEvents.GoToProductAnalogs -> {
                findNavController().navigateToProductAnalogs(event.productId)
            }

            is PastPurchasesFlowViewModel.PastPurchasesEvents.GoToProductDetails -> {
                findNavController().navigateToProductDetails(event.productId)
            }

            PastPurchasesFlowViewModel.PastPurchasesEvents.GoToCatalog -> {
                findNavController().popBackStack()
                tabManager.selectTab(R.id.graph_catalog)
            }
        }

    }
}
