package com.vodovoz.app.feature.past_purchases

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import com.vodovoz.app.common.cart.CartManager
import com.vodovoz.app.common.like.LikeManager
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.core.navigation.navigateToProductAnalogs
import com.vodovoz.app.core.navigation.navigateToPreOrder
import com.vodovoz.app.core.navigation.navigateToProductDetails
import com.vodovoz.app.core.navigation.navigateToSearch
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.effects.LifecycleEffect
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
                    val viewState by rememberUpdatedState(pagingState)
                    val lazyGridState = rememberLazyGridState()

                    PastPurchasesScreen(
                        viewModel = viewModel,
                        viewState = viewState,
                        lazyGridState = lazyGridState
                    )

                    LifecycleEffect {
                        viewModel.listenCart()
                    }

                    LifecycleEffect {
                        viewModel.listenProductLoadings()
                    }

                    LifecycleEffect {
                        viewModel.listenFavorites()
                    }


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
