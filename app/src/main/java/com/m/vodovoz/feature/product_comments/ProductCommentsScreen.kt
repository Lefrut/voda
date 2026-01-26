package com.m.vodovoz.feature.product_comments

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.SizeResolver
import coil3.video.videoFrameMillis
import com.m.vodovoz.R
import com.m.vodovoz.design_system.composables.button.VodovozButton
import com.m.vodovoz.design_system.composables.card.CommentCard
import com.m.vodovoz.design_system.composables.chip.VodovozChip
import com.m.vodovoz.design_system.composables.floating.BottomFloatingContainer
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.tab_row.VodovozScrollableTabRow
import com.m.vodovoz.design_system.composables.top_bar.VodovozTopBar
import com.m.vodovoz.feature.product_comments.model.CommentImage
import com.m.vodovoz.feature.product_comments.model.CommentMediaUi
import com.m.vodovoz.feature.product_comments.model.ProductCommentsInfoUi
import com.m.vodovoz.util.extensions.indexOfOrNull
import java.math.RoundingMode

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ProductCommentsScreen(
    viewModel: ProductCommentsFlowViewModel,
    viewState: ProductCommentsFlowViewModel.ProductCommentsState,
    lazyListState: LazyListState,
    sharedTransitionScope: SharedTransitionScope,
) {

    val aboutComments = viewState.productCommentsInfo
    val lazyPagingComments = viewState.pagedComments.collectAsLazyPagingItems()
    val loadState = lazyPagingComments.loadState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .consumeWindowInsets(WindowInsets.systemBars)
    ) {
        VodovozTopBar(
            title = stringResource(id = R.string.comments),
            onBack = {
                viewModel.navigateBack()
            }
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
            state = lazyListState
        ) {

            val sorting = aboutComments.sorting
            item {
                if (sorting.isNotEmpty()) {

                    VodovozScrollableTabRow(
                        selectedTabIndex = sorting.indexOfOrNull(viewState.currentSort) ?: 0,
                        spacing = 8.dp,
                        edgePadding = 16.dp
                    ) {
                        sorting.forEach { sort ->
                            VodovozChip(
                                text = sort.name,
                                selected = sort == viewState.currentSort,
                                onSelect = { viewModel.selectSort(sort) }
                            )
                        }
                    }
                }
            }

            item {
                CommentsInfoCard(
                    aboutComments = aboutComments,
                    modifier = Modifier.padding(
                        start = 16.dp,
                        top = 16.dp,
                        end = 16.dp
                    )
                )
            }



            item {
                if (aboutComments.media.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .horizontalScroll(rememberScrollState())
                            .padding(start = 16.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        aboutComments.media.forEach { media ->

                            val mediaUrl = media.url

                            key(mediaUrl) {
                                CommentImage(
                                    imageWidth = 80.dp,
                                    imageHeight = 110.dp,
                                    media = media,
                                    sharedTransitionScope = if (viewState.commentMedia == null) sharedTransitionScope else null,
                                    onClick = {
                                        viewModel.setFullScreenMedia(media)
                                    }
                                )
                            }
                        }
                    }
                }
            }


            if (loadState.refresh is LoadState.Loading) {
                item {
                    LoadingPlaceholder(modifier = Modifier.padding(top = 16.dp))
                }
            } else {

                items(
                    count = lazyPagingComments.itemCount
                ) { i ->
                    val comment = lazyPagingComments[i]
                    if (comment != null) {
                        CommentCard(
                            modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
                            comment = comment,
                            minLines = 1,
                            sharedTransitionScope = sharedTransitionScope,
                            onMediaClick = { media ->
                                viewModel.setFullScreenMedia(media)
                            }
                        )
                    }
                }
            }
            if (loadState.append is LoadState.Loading) {
                item {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .fillMaxWidth()
                            .wrapContentSize(align = Alignment.Center)
                            .size(30.dp),
                        strokeWidth = 3.dp,
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = Color.Transparent
                    )
                }
            }
        }

        AnimatedVisibility(viewState.showWriteComment) {
            BottomFloatingContainer {
                VodovozButton(
                    text = stringResource(id = R.string.write_comment_btn_text),
                    onClick = { viewModel.navigateToWriteComment() },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun CommentsInfoCard(modifier: Modifier = Modifier, aboutComments: ProductCommentsInfoUi) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            Text(
                text = stringResource(
                    R.string.rating_value,
                    try {
                        aboutComments.ratingText.toBigDecimal()
                            .setScale(2, RoundingMode.HALF_UP)
                            .stripTrailingZeros()
                            .toString()
                    } catch (_: Throwable) {
                        aboutComments.ratingText
                    }

                ),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_star_active),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(18.dp),
                tint = MaterialTheme.colorScheme.tertiary
            )
        }
        Text(
            modifier = Modifier.padding(top = 4.dp),
            text = aboutComments.commentsCountText,
            color = MaterialTheme.colorScheme.surfaceTint,
            style = MaterialTheme.typography.bodySmall
        )
    }
}