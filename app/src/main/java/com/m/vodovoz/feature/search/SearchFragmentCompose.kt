package com.m.vodovoz.feature.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.m.vodovoz.ui.mvi.collectAsState
import androidx.navigation.fragment.findNavController
import com.m.vodovoz.R
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.core.navigation.navigateToProductAnalogs
import com.m.vodovoz.core.navigation.navigateToPreOrder
import com.m.vodovoz.core.navigation.navigateToProductDetails
import com.m.vodovoz.core.navigation.navigateToPromotions
import com.m.vodovoz.core.navigation.navigateToSearchProductList
import com.m.vodovoz.core.navigation.navigateToWebView
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.core.navigation.ContentSearchNavigator
import com.m.vodovoz.ui.insets.InsetsVisibilityState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : Fragment() {

    internal val viewModel: SearchFlowViewModel by viewModels()

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
        insetsVisibilityState.consumeSystemBarInsets(true)
    }

    override fun onStop() {
        super.onStop()
        tabManager.changeTabVisibility(true)
        insetsVisibilityState.consumeSystemBarInsets(true)
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
                    val viewState by viewModel.collectAsState()
                    

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
