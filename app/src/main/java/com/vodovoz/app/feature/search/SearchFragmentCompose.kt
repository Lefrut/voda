package com.vodovoz.app.feature.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.R
import com.vodovoz.app.common.cart.CartManager
import com.vodovoz.app.common.like.LikeManager
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.core.navigation.navigateToProductAnalogs
import com.vodovoz.app.core.navigation.navigateToPreOrder
import com.vodovoz.app.core.navigation.navigateToProductDetails
import com.vodovoz.app.core.navigation.navigateToPromotions
import com.vodovoz.app.core.navigation.navigateToSearchProductList
import com.vodovoz.app.core.navigation.navigateToWebView
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.core.navigation.ContentSearchNavigator
import com.vodovoz.app.ui.insets.InsetsVisibilityState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : Fragment() {

    internal val viewModel: SearchFlowViewModel by viewModels()

    @Inject
    lateinit var cartManager: CartManager

    @Inject
    lateinit var likeManager: LikeManager

    @Inject
    lateinit var tabManager: TabManager

    @Inject
    lateinit var insetsVisibilityState: InsetsVisibilityState

    @Inject
    lateinit var navigatorFactory: ContentSearchNavigator.Factory

    private lateinit var searchNavigator: ContentSearchNavigator

    override fun onAttach(context: Context) {
        super.onAttach(context)
        searchNavigator = navigatorFactory.create(
            findNavController(), this
        )
    }

    override fun onStart() {
        super.onStart()
        insetsVisibilityState.insertSystemBarInsets(true)
    }

    override fun onStop() {
        super.onStop()
        tabManager.changeTabVisibility(true)
        insetsVisibilityState.insertSystemBarInsets(true)
    }

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

                    when (viewState.uiState) {
                        SearchFlowViewModel.UiState.Error -> {
                            NetworkErrorPlaceholder { viewModel.retrySearchQuery() }
                        }

                        else -> {
                            SearchScreen(viewModel = viewModel, viewState = viewState)
                        }
                    }

                    LifecycleEffect {
                        listenEvents()
                    }

                    LifecycleEffect {
                        viewModel.listenCart()
                    }

                    LifecycleEffect {
                        viewModel.listenProductLoadings()
                    }

                    LifecycleEffect {
                        viewModel.listenSearchHistory()
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            tabManager.changeTabVisibility(!imeVisible)
            return@setOnApplyWindowInsetsListener insets
        }

    }

    private suspend fun listenEvents() {
        viewModel.events.collect { event ->
            when (event) {
                is SearchFlowViewModel.SearchEvents.GoToPreOrder -> {
                    findNavController().navigateToPreOrder(event.productId)
                }

                is SearchFlowViewModel.SearchEvents.GoToProfile -> {
                    tabManager.setAuthRedirect(findNavController().graph.id)
                    tabManager.selectTab(R.id.graph_profile)
                }

                SearchFlowViewModel.SearchEvents.GoToContacts -> {
                    //findNavController().navigate(SearchFragmentDirections.actionToContactsFragment())
                }

                SearchFlowViewModel.SearchEvents.GoToPromotions -> {
                    findNavController().navigateToPromotions()
                }

                is SearchFlowViewModel.SearchEvents.GoToService -> {
                }

                is SearchFlowViewModel.SearchEvents.GoToWebView -> {
                    findNavController().navigateToWebView(
                        event.url,
                        event.title
                    )
                }

                SearchFlowViewModel.SearchEvents.GoBack -> {
                    findNavController().popBackStack()
                }

                is SearchFlowViewModel.SearchEvents.GoToSearchProductList -> {
                    findNavController().navigateToSearchProductList(event.query)
                }

                SearchFlowViewModel.SearchEvents.GoToScanner -> {
                    searchNavigator.navigateToImageSearch()
                }

                is SearchFlowViewModel.SearchEvents.GoToProductDetails -> {
                    findNavController().navigateToProductDetails(event.productId)
                }

                is SearchFlowViewModel.SearchEvents.GoToProductAnalogs -> {
                    findNavController().navigateToProductAnalogs(event.productId)
                }
            }
        }
    }

}
