package com.m.vodovoz.feature.stories_fragment

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleStartEffect
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.core.navigation.activate
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.effects.AppearanceSystemBarsEffect
import com.m.vodovoz.feature.stories_fragment.api.StoriesNavKey
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.ui.mvi.collectEvents
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun StoriesEntry(navKey: StoriesNavKey? = null) =
    NavigationEntry<StoriesViewModel, StoriesViewModel.Factory>(
        creationCallback = { factory -> factory.create(navKey) }
    ) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val viewState by viewModel.collectAsState()

    LifecycleStartEffect(Unit) {
        viewModel.tabManager.changeTabVisibility(false)
        coroutineScope.launch {
            delay(100)
            viewModel.insetsVisibilityState.consumeSystemBarInsets(false)
        }
        onStopOrDispose {
            viewModel.insetsVisibilityState.consumeSystemBarInsets(true)
            viewModel.tabManager.changeTabVisibility(true)
        }
    }

    AppearanceSystemBarsEffect(
        lightNavigationBar = false,
        lightStatusBar = false
    )

    val pagerState = rememberPagerState(viewState.currentStoryIndex) { viewState.stories.size }

    Crossfade(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.onBackground),
        targetState = viewState.uiState,
        label = "stories cross fade"
    ) { uiState ->
        when (uiState) {
            StoriesViewModel.StoriesUiState.Success,
            StoriesViewModel.StoriesUiState.Loading -> {
                if (!pagerState.isScrollInProgress) {
                    LoadingPlaceholder(
                        containerColor = MaterialTheme.colorScheme.onBackground
                    )
                }
                StoriesScreen(
                    viewState = viewState,
                    viewModel = viewModel,
                    pagerState = pagerState
                )
            }
        }
    }

    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress) {
            viewModel.changeStoryIndex(pagerState.currentPage)
        } else {
            viewModel.stopStory()
        }
    }

    viewModel.collectEvents { event ->
        when (event) {
            is StoriesViewModel.StoriesEvents.ChangePagerIndex -> {
                coroutineScope.launch { pagerState.animateScrollToPage(event.newStoryIndex) }
            }

            StoriesViewModel.StoriesEvents.GoBack -> {
                navigator.goBack()
            }

            is StoriesViewModel.StoriesEvents.ActivateAction -> {
                val cookie = viewModel.cookieManager.fetchCookieSessionId() ?: ""
                event.action.activate(
                    navigator = navigator,
                    context = context,
                    cookie = cookie,
                    tabManager = viewModel.tabManager
                )
            }
        }
    }
}
