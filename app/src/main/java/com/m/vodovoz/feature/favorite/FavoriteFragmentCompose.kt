package com.m.vodovoz.feature.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.m.vodovoz.R
import com.m.vodovoz.common.cart.CartManager
import com.m.vodovoz.common.like.LikeManager
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.core.navigation.navigateToProductAnalogs
import com.m.vodovoz.core.navigation.navigateToCategories
import com.m.vodovoz.core.navigation.navigateToProductDetails
import com.m.vodovoz.core.navigation.navigateToSearch
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.home.model.CategoryUi
import com.m.vodovoz.ui.mvi.collectAsState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FavoriteFragment : Fragment() {


    internal val viewModel: FavoriteFlowViewModel by activityViewModels()

    @Inject
    lateinit var cartManager: CartManager

    @Inject
    lateinit var likeManager: LikeManager

    @Inject
    lateinit var tabManager: TabManager

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.fetchFavoritesIfChanges()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        findNavController().currentBackStackEntry?.savedStateHandle?.remove<CategoryUi>(
            "category"
        )?.let { category -> viewModel.selectCategory(category) } ?: viewModel.fetchFavoritesIfChanges()

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                VodovozTheme {
                    val viewState by viewModel.collectAsState()
                    
                    val lazyGridState = rememberLazyGridState()

                    when (viewState.uiState) {
                        FavoriteFlowViewModel.FavoriteUiState.Error -> {
                            NetworkErrorPlaceholder(
                                onTryAgainClick = {
                                    viewModel.fetchFavoriteProducts()
                                }
                            )
                        }

                        else -> {
                            FavoriteScreen(
                                viewModel = viewModel,
                                viewState = viewState,
                                lazyGridState = lazyGridState
                            )
                        }
                    }

                    LifecycleEffect {
                        observeEvents(lazyGridState)
                    }
                }
            }
        }
    }

    private suspend fun observeEvents(lazyGridState: LazyGridState) {
        viewModel.events
            .collect { event ->
                when (event) {
                    is FavoriteFlowViewModel.FavoriteEvents.GoToProfile -> {
                        tabManager.setAuthRedirect(findNavController().graph.id)
                        tabManager.selectTab(R.id.graph_profile)
                    }

                    is FavoriteFlowViewModel.FavoriteEvents.GoToCategories -> {
                        findNavController().navigateToCategories(
                            categories = event.categories,
                            category = event.category,
                        )
                    }

                    is FavoriteFlowViewModel.FavoriteEvents.GoToProductDetails -> {
                        findNavController().navigateToProductDetails(event.productId)
                    }

                    FavoriteFlowViewModel.FavoriteEvents.ScrollToTop -> {
                        lazyGridState.animateScrollToItem(0)
                    }

                    FavoriteFlowViewModel.FavoriteEvents.GoToSearch -> {
                        findNavController().navigateToSearch()
                    }

                    FavoriteFlowViewModel.FavoriteEvents.GoToCatalog -> {
                        tabManager.selectTab(R.id.graph_catalog)
                        findNavController().popBackStack(R.id.catalogFragment, false)
                    }

                    is FavoriteFlowViewModel.FavoriteEvents.GoToProductAnalogs -> {
                        findNavController().navigateToProductAnalogs(event.productId)
                    }
                }
            }


    }

}
