package com.vodovoz.app.feature.product_details.detail_media

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.feature.product_details.detail_media.model.DetailMediaEvent
import com.vodovoz.app.util.extensions.disableFullScreen
import com.vodovoz.app.util.extensions.enableFullScreen
import com.vodovoz.app.util.extensions.indexOfOrNull
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@AndroidEntryPoint
class DetailMediaFragment : Fragment() {

    private val viewModel: DetailMediaViewModel by viewModels()

    @Inject
    lateinit var tabManager: TabManager

    override fun onStart() {
        super.onStart()
        tabManager.changeTabVisibility(false)
    }

    override fun onStop() {
        super.onStop()
        tabManager.changeTabVisibility(true)
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
                    val viewState by viewModel.state.collectAsStateWithLifecycle()
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
                                    activity.requestedOrientation =
                                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                    activity.enableFullScreen()

                                }

                                DetailMediaEvent.MakePortrait -> {
                                    val activity = requireActivity()
                                    activity.requestedOrientation =
                                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                    activity.disableFullScreen()

                                }
                            }
                        }
                    }
                }
            }
        }
    }

}