package com.m.vodovoz.feature.product_comments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.m.vodovoz.ui.mvi.collectAsState
import androidx.navigation.fragment.findNavController
import com.m.vodovoz.R
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.core.navigation.navigateToWriteComment
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.composables.VerticalImagePager
import com.m.vodovoz.design_system.composables.decoration.LocalShimmer
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.product_comments.model.CommentMediaUi
import com.m.vodovoz.ui.compose.player.MediaComposePlayer
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
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
                val viewState by viewModel.collectAsState()
                val lazyListState = rememberLazyListState()

                VodovozTheme {
                    CompositionLocalProvider(LocalShimmer provides rememberShimmer(ShimmerBounds.View)) {
                        SharedTransitionLayout {
                            ProductCommentsScreen(
                                viewModel = viewModel,
                                viewState = viewState,
                                lazyListState = lazyListState,
                                sharedTransitionScope = this
                            )

                            val currentMedia = viewState.commentMedia

                            when (currentMedia) {
                                null -> {

                                }

                                else -> {
                                    BackHandler {
                                        viewModel.resetFullScreenMedia()
                                    }
                                }
                            }

                            when (currentMedia) {
                                is CommentMediaUi.Image -> {
                                    val images =
                                        viewState.productCommentsInfo.media.mapNotNull { image ->
                                            image as? CommentMediaUi.Image
                                        }
                                    VerticalImagePager(
                                        initialPage = images.indexOf(currentMedia),
                                        images = images,
                                        sharedTransitionScope = this@SharedTransitionLayout,
                                        onCloseClick = {
                                            viewModel.resetFullScreenMedia()
                                        }
                                    )

                                }

                                is CommentMediaUi.Video -> {
                                    Box {
                                        MediaComposePlayer(
                                            url = currentMedia.url,
                                            modifier = Modifier.clickable {}
                                        )


                                        Icon(
                                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_close),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(
                                                    end = 16.dp,
                                                    top = 32.dp
                                                )
                                                .clip(CircleShape)
                                                .clickable {
                                                    viewModel.resetFullScreenMedia()
                                                }
                                                .background(MaterialTheme.colorScheme.surface)
                                                .padding(8.dp)
                                                .size(32.dp)
                                                .zIndex(Float.MAX_VALUE),
                                            tint = MaterialTheme.colorScheme.onBackground
                                        )

                                    }
                                }

                                null -> {

                                }
                            }
                        }
                    }



                    LifecycleEffect {
                        viewModel.events.collect { event ->
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
