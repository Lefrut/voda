package com.vodovoz.app.feature.stories_fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.common.cookie.CookieManager
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.core.ui.activate
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.composables.placeholders.LoadingPlaceholder
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.design_system.effects.SystemBarsEffect
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StoriesFragment : Fragment() {

    private val viewModel: StoriesViewModel by viewModels()

    @Inject
    lateinit var tabManager: TabManager

    @Inject
    lateinit var cookieManager: CookieManager

    @SuppressLint("UnsafeRepeatOnLifecycleDetector")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)

            setContent {
                DisposableEffect(Unit) {
                    tabManager.changeTabVisibility(false)
                    onDispose { tabManager.changeTabVisibility(true) }
                }

                VodovozTheme {
                    val pagingState by viewModel.observeUiState().collectAsStateWithLifecycle()
                    val viewState by rememberUpdatedState(pagingState.data)

                    val pagerState =
                        if (viewState.uiState !is StoriesViewModel.StoriesUiState.Success) {
                            rememberPagerState(viewState.currentStoryIndex) { viewState.stories.size }
                        } else {
                            rememberPagerState(viewState.currentStoryIndex) { viewState.stories.size }
                        }


                    when (viewState.uiState) {
                        StoriesViewModel.StoriesUiState.Loading -> {
                            LoadingPlaceholder(
                                modifier = Modifier,
                                containerColor = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        StoriesViewModel.StoriesUiState.Success -> {
                            StoriesScreen(
                                viewState = viewState,
                                viewModel = viewModel,
                                pagerState = pagerState
                            )
                        }
                    }




                    SystemBarsEffect(
                        statusBarColor = MaterialTheme.colorScheme.onBackground,
                        navigationBarColor = MaterialTheme.colorScheme.onBackground,
                        handleDecorFitsSystemWindows = false
                    )

                    LaunchedEffect(pagerState.currentPage) {
                        viewModel.changeStoryIndex(pagerState.currentPage)
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