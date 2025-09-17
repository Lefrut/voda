package com.m.vodovoz.feature.product_details.detail_media

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.m.vodovoz.ui.mvi.collectAsState
import androidx.navigation.fragment.findNavController
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.product_details.detail_media.model.DetailMediaEvent
import com.m.vodovoz.ui.insets.InsetsVisibilityState
import com.m.vodovoz.util.extensions.disableFullScreen
import com.m.vodovoz.util.extensions.enableFullScreen
import com.m.vodovoz.util.extensions.indexOfOrNull
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@AndroidEntryPoint
class DetailMediaFragment : Fragment() {

    private val viewModel: DetailMediaViewModel by viewModels()

    @Inject
    lateinit var tabManager: TabManager

    @Inject
    lateinit var insetsVisibilityState: InsetsVisibilityState

    override fun onStart() {
        super.onStart()
        tabManager.changeTabVisibility(false)
    }

    override fun onStop() {
        super.onStop()
        insetsVisibilityState.consumeSystemBarInsets(false)
        insetsVisibilityState.consumeSystemBarInsets(true)
        tabManager.changeTabVisibility(true)
    }

    @SuppressLint("SourceLockedOrientationActivity")
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
                    val mediaList = viewState.mediaList

                    if (mediaList.isNotEmpty()) {


                        val pagerState = rememberPagerState(
                            mediaList.indexOfOrNull(viewState.currentMedia) ?: 0
                        ) { mediaList.size }


                        DetailMediaScreen(
                            viewModel = viewModel,
                            viewState = viewState,
                            pagerState = pagerState
                        )

                        LaunchedEffect(pagerState) {
                            snapshotFlow { pagerState.currentPage }.distinctUntilChanged()
                                .collectLatest { currentPage ->
                                    viewModel.setMediaByIndex(currentPage)
                                    findNavController().previousBackStackEntry?.savedStateHandle?.set(
                                        "mediaIndex",
                                        currentPage
                                    )
                                }
                        }
                    }


                    LifecycleEffect {
                        viewModel.events.collect { event ->
                            when (event) {
                                DetailMediaEvent.GoBack -> {
                                    findNavController().popBackStack()
                                }

                                DetailMediaEvent.MakeLandscape -> {
                                    val activity = requireActivity()
                                    activity.enableFullScreen()
                                    insetsVisibilityState.consumeSystemBarInsets(false)
                                    activity.requestedOrientation =
                                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                }

                                DetailMediaEvent.MakePortrait -> {
                                    val activity = requireActivity()
                                    activity.disableFullScreen()
                                    insetsVisibilityState.consumeSystemBarInsets(true)
                                    activity.requestedOrientation =
                                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}