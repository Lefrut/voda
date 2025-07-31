package com.vodovoz.app.feature.stories_fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.common.cookie.CookieManager
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.core.navigation.activate
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.composables.placeholders.LoadingPlaceholder
import com.vodovoz.app.design_system.effects.AppearanceSystemBarsEffect
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.ui.insets.InsetsVisibilityState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StoriesFragment : Fragment() {

    private val viewModel: StoriesViewModel by viewModels()

    @Inject
    lateinit var tabManager: TabManager

    @Inject
    lateinit var cookieManager: CookieManager

    @Inject
    lateinit var insetsVisibilityState: InsetsVisibilityState

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                val coroutineScope = rememberCoroutineScope()

                LifecycleStartEffect(Unit) {
                    tabManager.changeTabVisibility(false)
                    coroutineScope.launch {
                        delay(100)
                        insetsVisibilityState.insertSystemBarInsets(false)
                    }
                    onStopOrDispose {
                        insetsVisibilityState.insertSystemBarInsets(true)
                        tabManager.changeTabVisibility(true)
                    }
                }

                AppearanceSystemBarsEffect(
                    lightNavigationBar = false,
                    lightStatusBar = false
                )

                VodovozTheme {
                    val pagingState by viewModel.observeUiState().collectAsStateWithLifecycle()
                    val viewState by rememberUpdatedState(pagingState.data)

                    val pagerState =
                        if (viewState.uiState !is StoriesViewModel.StoriesUiState.Success) {
                            rememberPagerState(viewState.currentStoryIndex) { viewState.stories.size }
                        } else {
                            rememberPagerState(viewState.currentStoryIndex) { viewState.stories.size }
                        }


                    Crossfade(
                        targetState = viewState.uiState,
                        label = "stories cross fade"
                    ) { uiState ->
                        when (uiState) {
                            StoriesViewModel.StoriesUiState.Success, StoriesViewModel.StoriesUiState.Loading -> {
                                LoadingPlaceholder(
                                    containerColor = MaterialTheme.colorScheme.onBackground
                                )
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



                    LifecycleEffect(pagerState) {
                        viewModel.observeEvent().collect { event ->
                            when (event) {
                                is StoriesViewModel.StoriesEvents.ChangePagerIndex -> {
                                    launch { pagerState.animateScrollToPage(event.newStoryIndex) }
                                }

                                StoriesViewModel.StoriesEvents.GoBack -> {
                                    val navController = findNavController()
                                    navController.popBackStack()
                                }

                                is StoriesViewModel.StoriesEvents.ActivateAction -> {
                                    val cookie = cookieManager.fetchCookieSessionId() ?: ""
                                    event.action.activate(
                                        navController = findNavController(),
                                        context = requireContext(),
                                        cookie = cookie,
                                        tabManager = tabManager
                                    )
                                }
                            }
                        }
                    }

                }
            }
        }
    }


}