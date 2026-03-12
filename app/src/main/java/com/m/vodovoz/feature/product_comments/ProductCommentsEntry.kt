package com.m.vodovoz.feature.product_comments

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.paging.compose.collectAsLazyPagingItems
import com.m.vodovoz.common.tab.hideTab
import com.m.vodovoz.common.tab.showTab
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.core.navigation.navigateToWriteComment
import com.m.vodovoz.design_system.composables.VerticalImagePager
import com.m.vodovoz.design_system.composables.decoration.LocalShimmer
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.product_comments.model.CommentMediaUi
import com.m.vodovoz.ui.compose.player.MediaComposePlayer
import com.m.vodovoz.ui.mvi.collectAsState
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ProductCommentsEntry() = NavigationEntry<ProductCommentsFlowViewModel> {
    val viewState by viewModel.collectAsState()
    val lazyCommentColumnState = rememberLazyListState()
    val lazyMediaRowState = rememberLazyListState()
    val lazyPagingComments = viewState.pagedComments.collectAsLazyPagingItems()

    CompositionLocalProvider(LocalShimmer provides rememberShimmer(ShimmerBounds.View)) {
        SharedTransitionLayout {
            AnimatedContent(
                targetState = viewState.commentMedia,
                transitionSpec = {
                    EnterTransition.None togetherWith ExitTransition.None
                }
            ) { currentMedia ->

                if (currentMedia != null) {
                    BackHandler {
                        viewModel.resetFullScreenMedia()
                    }
                }

                when (currentMedia) {
                    is CommentMediaUi.Image -> {
                        val images = viewState.productCommentsInfo.media.mapNotNull { image ->
                            image as? CommentMediaUi.Image
                        }

                        DisposableEffect(Unit) {
                            viewModel.tabManager.hideTab()
                            viewModel.insetsVisibilityState.consumeStatusBarInsets(false)

                            onDispose {
                                viewModel.insetsVisibilityState.consumeStatusBarInsets(true)
                                viewModel.tabManager.showTab()
                            }
                        }

                        VerticalImagePager(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.onBackground)
                                .fillMaxSize()
                                .windowInsetsPadding(WindowInsets.systemBars),
                            initialPage = images.indexOf(currentMedia),
                            images = images,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedContentScope = this,
                            onCloseClick = {
                                viewModel.resetFullScreenMedia()
                            }
                        )
                    }

                    is CommentMediaUi.Video -> {
                        MediaComposePlayer(
                            url = currentMedia.url,
                            modifier = Modifier.clickable {},
                            onCloseClick = {
                                viewModel.resetFullScreenMedia()
                            }
                        )
                    }

                    null -> {
                        ProductCommentsScreen(
                            viewModel = viewModel,
                            viewState = viewState,
                            lazyCommentsListState = lazyCommentColumnState,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedContentScope = this,
                            lazyPagingComments = lazyPagingComments,
                            lazyMediaListState = lazyMediaRowState
                        )
                    }
                }
            }
        }
    }

    LifecycleEffect {
        viewModel.events.collect { event ->
            when (event) {
                ProductCommentsFlowViewModel.ProductCommentsEvents.ScrollToTop -> {
                    lazyCommentColumnState.animateScrollToItem(0)
                }

                ProductCommentsFlowViewModel.ProductCommentsEvents.GoBack -> {
                    navigator.goBack()
                }

                is ProductCommentsFlowViewModel.ProductCommentsEvents.GoToWriteComment -> {
                    navigator.navigateToWriteComment(
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
