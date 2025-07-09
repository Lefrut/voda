package com.vodovoz.app.feature.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.R
import com.vodovoz.app.common.cart.CartManager
import com.vodovoz.app.common.like.LikeManager
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.core.navigation.navigateToProductAnalogs
import com.vodovoz.app.core.navigation.navigateToCategories
import com.vodovoz.app.core.navigation.navigateToProductDetails
import com.vodovoz.app.core.navigation.navigateToSearch
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.feature.home.model.CategoryUi
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
        viewModel.checkFavoritesChanges()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        findNavController().currentBackStackEntry?.savedStateHandle?.remove<CategoryUi>(
            "category"
        )?.let { category -> viewModel.selectCategory(category) } ?: viewModel.checkFavoritesChanges()

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                VodovozTheme {
                    val pagingState by viewModel.observeUiState().collectAsStateWithLifecycle()
                    val viewState by rememberUpdatedState(pagingState.data)
                    val lazyGridState = rememberLazyGridState()

                    when (viewState.uiState) {
                        FavoriteFlowViewModel.FavoriteUiState.Error -> {
                            NetworkErrorPlaceholder(
                                onTryAgainClick = { viewModel.fetchFavoriteProducts() }
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

                    LifecycleEffect {
                        viewModel.listenCart()
                    }

                    LifecycleEffect {
                        viewModel.listenProductLoadings()
                    }

                }
            }
        }
    }

    private suspend fun observeEvents(lazyGridState: LazyGridState) {
        viewModel.observeEvent()
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
