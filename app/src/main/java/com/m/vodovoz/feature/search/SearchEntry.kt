package com.m.vodovoz.feature.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.compose.LifecycleStartEffect
import com.m.vodovoz.R
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.core.navigation.navigateToProductAnalogs
import com.m.vodovoz.core.navigation.navigateToPreOrder
import com.m.vodovoz.core.navigation.navigateToProductDetails
import com.m.vodovoz.core.navigation.navigateToPromotions
import com.m.vodovoz.core.navigation.navigateToQrCode
import com.m.vodovoz.core.navigation.navigateToSearchProductList
import com.m.vodovoz.core.navigation.navigateToSpeechDialog
import com.m.vodovoz.core.navigation.navigateToWebView
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.search.api.SearchNavKey
import com.m.vodovoz.ui.mvi.collectAsState

@Composable
fun SearchEntry(navKey: SearchNavKey? = null) =
    NavigationEntry<SearchFlowViewModel, SearchFlowViewModel.Factory>(
        creationCallback = { factory -> factory.create(navKey) }
    ) {
    val view = LocalView.current
    val viewState by viewModel.collectAsState()

    LifecycleStartEffect(Unit) {
        viewModel.insetsVisibilityState.consumeSystemBarInsets(true)
        onStopOrDispose {
            viewModel.tabManager.changeTabVisibility(true)
            viewModel.insetsVisibilityState.consumeSystemBarInsets(true)
        }
    }

    DisposableEffect(view) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            viewModel.tabManager.changeTabVisibility(!imeVisible)
            insets
        }
        onDispose {
            ViewCompat.setOnApplyWindowInsetsListener(view, null)
        }
    }

    when (viewState.uiState) {
        SearchFlowViewModel.UiState.Error -> {
            NetworkErrorPlaceholder { viewModel.retrySearchQuery() }
        }

        else -> {
            SearchScreen(viewModel = viewModel, viewState = viewState)
        }
    }

    LifecycleEffect {
        viewModel.events.collect { event ->
            when (event) {
                is SearchFlowViewModel.SearchEvents.GoToPreOrder -> {
                    navigator.navigateToPreOrder(event.productId)
                }

                is SearchFlowViewModel.SearchEvents.GoToProfile -> {
                    viewModel.tabManager.setAuthRedirect(navigator.graph.id)
                    viewModel.tabManager.selectTab(R.id.graph_profile)
                }

                SearchFlowViewModel.SearchEvents.GoToContacts -> Unit

                SearchFlowViewModel.SearchEvents.GoToPromotions -> {
                    navigator.navigateToPromotions()
                }

                is SearchFlowViewModel.SearchEvents.GoToService -> Unit

                is SearchFlowViewModel.SearchEvents.GoToWebView -> {
                    navigator.navigateToWebView(event.url, event.title)
                }

                SearchFlowViewModel.SearchEvents.GoBack -> {
                    navigator.goBack()
                }

                is SearchFlowViewModel.SearchEvents.GoToSearchProductList -> {
                    navigator.navigateToSearchProductList(event.query)
                }

                SearchFlowViewModel.SearchEvents.GoToScanner -> {
                    navigator.navigateToQrCode()
                }

                is SearchFlowViewModel.SearchEvents.GoToProductDetails -> {
                    navigator.navigateToProductDetails(event.productId)
                }

                is SearchFlowViewModel.SearchEvents.GoToProductAnalogs -> {
                    navigator.navigateToProductAnalogs(event.productId)
                }
            }
        }
    }

    LifecycleEffect {
        viewModel.listenSearchHistory()
    }
}
