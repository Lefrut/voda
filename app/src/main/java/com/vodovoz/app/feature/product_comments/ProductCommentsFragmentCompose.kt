package com.vodovoz.app.feature.product_comments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.core.navigation.navigateToWriteComment
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.composables.VerticalImagePager
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.util.extensions.indexOfOrNull
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProductCommentsFragment : Fragment() {

    private val viewModel: ProductCommentsFlowViewModel by viewModels()

    @Inject
    lateinit var tabManager: TabManager

    @OptIn(ExperimentalSharedTransitionApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                val pagingState by viewModel.observeUiState().collectAsStateWithLifecycle()
                val viewState by rememberUpdatedState(pagingState.data)
                val lazyListState = rememberLazyListState()

                VodovozTheme {
                    SharedTransitionLayout {
                        ProductCommentsScreen(
                            viewModel = viewModel,
                            viewState = viewState,
                            lazyListState = lazyListState,
                            sharedTransitionScope = this
                        )

                        if (viewState.fullScreenImage != null) {
                            DisposableEffect(Unit) {
                                tabManager.changeTabVisibility(false)
                                onDispose {
                                    tabManager.changeTabVisibility(true)
                                }
                            }

                            BackHandler {
                                viewModel.resetFullScreenImage()
                            }

                            val images = viewState.productCommentsInfo.images
                            VerticalImagePager(
                                initialPage = images.indexOfOrNull(
                                    viewState.fullScreenImage
                                ) ?: 0,
                                images = images,
                                sharedTransitionScope = this@SharedTransitionLayout,
                                onCloseClick = {
                                    viewModel.resetFullScreenImage()
                                }
                            )
                        }
                    }



                    LifecycleEffect {
                        viewModel.observeEvent().collect { event ->
                            when (event) {
                                ProductCommentsFlowViewModel.ProductCommentsEvents.ScrollToTop -> {
                                    lazyListState.animateScrollToItem(0)
                                }


                                ProductCommentsFlowViewModel.ProductCommentsEvents.GoBack -> {
                                    findNavController().popBackStack()
                                }

                                is ProductCommentsFlowViewModel.ProductCommentsEvents.GoToWriteComment -> {
                                    findNavController().navigateToWriteComment(
                                        productId = event.productId,
                                        productImage = event.productImage,
                                        productName = event.productName,
                                        rating = 0
                                    )
                                }
                            }

                        }
                    }

                    BackHandler { viewModel.navigateBack() }
                }

            }
        }
    }
}
