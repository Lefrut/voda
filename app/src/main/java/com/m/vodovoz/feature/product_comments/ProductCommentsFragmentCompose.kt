package com.m.vodovoz.feature.product_comments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.m.vodovoz.core.navigation.navigateToCommentMedia
import com.m.vodovoz.core.navigation.navigateToWriteComment
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.composables.decoration.LocalShimmer
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.ui.mvi.collectAsState
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductCommentsFragment : Fragment() {

    private val viewModel: ProductCommentsFlowViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                val viewState by viewModel.collectAsState()
                val lazyCommentColumnState = rememberLazyListState()
                val lazyMediaRowState = rememberLazyListState()

                VodovozTheme {
                    CompositionLocalProvider(LocalShimmer provides rememberShimmer(ShimmerBounds.View)) {
                        ProductCommentsScreen(
                            viewModel = viewModel,
                            viewState = viewState,
                            lazyCommentsListState = lazyCommentColumnState,
                            lazyMediaListState = lazyMediaRowState
                        )
                    }

                    LifecycleEffect {
                        viewModel.events.collect { event ->
                            when (event) {
                                ProductCommentsFlowViewModel.ProductCommentsEvents.ScrollToTop -> {
                                    lazyCommentColumnState.animateScrollToItem(0)
                                }


                                ProductCommentsFlowViewModel.ProductCommentsEvents.GoBack -> {
                                    findNavController().popBackStack()
                                }

                                is ProductCommentsFlowViewModel.ProductCommentsEvents.GoToCommentMedia -> {
                                    findNavController().navigateToCommentMedia(event.media, event.mediaList)
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
