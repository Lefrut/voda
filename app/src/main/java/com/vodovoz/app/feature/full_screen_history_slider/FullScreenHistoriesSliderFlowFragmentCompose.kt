package com.vodovoz.app.feature.full_screen_history_slider

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.vodovoz.app.common.account.data.AccountManager
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.composables.placeholders.LoadingPlaceholder
import com.vodovoz.app.design_system.effects.LifecycleEffect
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FullScreenHistoriesSliderFlowFragment : Fragment() {


    private val viewModel: FullScreenHistoriesSliderFlowViewModel by viewModels()

    @Inject
    lateinit var tabManager: TabManager

    @Inject
    lateinit var accountManager: AccountManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.updateData()
    }

    override fun onStart() {
        super.onStart()
        tabManager.changeTabVisibility(false)

    }

    override fun onStop() {
        super.onStop()
        tabManager.changeTabVisibility(true)
    }

    @SuppressLint("UnsafeRepeatOnLifecycleDetector")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val navController = findNavController()
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                VodovozTheme {
                    val viewState by viewModel.observeUiState().collectAsStateWithLifecycle()
                    val data = viewState.data

                    val pagerState =
                        if (data.uiState !is FullScreenHistoriesSliderFlowViewModel.UiState.Success) rememberPagerState(
                            data.currentStoryIndex
                        ) { data.stories.size } else rememberPagerState(data.currentStoryIndex) { data.stories.size }


                    when (viewState.data.uiState) {
                        FullScreenHistoriesSliderFlowViewModel.UiState.Loading -> {
                            LoadingPlaceholder(
                                modifier = Modifier,
                                containerColor = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        FullScreenHistoriesSliderFlowViewModel.UiState.Success -> {
                            StoriesScreen(
                                viewState = viewState.data,
                                viewModel = viewModel,
                                pagerState = pagerState
                            )
                        }
                    }


                    val systemUiController = rememberSystemUiController()

                    val backgroundColor = MaterialTheme.colorScheme.background
                    val onBackgroundColor = MaterialTheme.colorScheme.onBackground

                    DisposableEffect(Unit) {
                        systemUiController.setSystemBarsColor(
                            color = onBackgroundColor,
                            isNavigationBarContrastEnforced = false
                        )
                        onDispose {
                            systemUiController.setSystemBarsColor(
                                color = backgroundColor,
                                isNavigationBarContrastEnforced = false
                            )
                        }
                    }



                    LaunchedEffect(pagerState.currentPage) {
                        viewModel.changeStoryIndex(pagerState.currentPage)
                    }



                    LifecycleEffect(arg2 = pagerState) {
                        viewModel.observeEvent().collect { event ->
                            when (event) {
                                is FullScreenHistoriesSliderFlowViewModel.HistoriesSliderEvents.ChangePagerIndex -> {
                                    launch { pagerState.animateScrollToPage(event.newStoryIndex) }
                                }

                                FullScreenHistoriesSliderFlowViewModel.HistoriesSliderEvents.GoBack -> {
                                    navController.popBackStack()
                                }

                                FullScreenHistoriesSliderFlowViewModel.HistoriesSliderEvents.GoToProfile -> {

                                }
                            }
                        }
                    }

                }
            }
        }
    }




}