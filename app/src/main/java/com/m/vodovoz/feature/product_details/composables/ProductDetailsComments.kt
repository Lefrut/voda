package com.m.vodovoz.feature.product_details.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.m.vodovoz.R
import com.m.vodovoz.design_system.composables.button.VodovozButton
import com.m.vodovoz.design_system.composables.button.VodovozButtonDefaults
import com.m.vodovoz.design_system.composables.card.CommentCard
import com.m.vodovoz.design_system.model.ButtonUi
import com.m.vodovoz.design_system.model.CommentUi
import com.m.vodovoz.feature.home.composables.TitleAndButton
import com.m.vodovoz.feature.product_comments.model.CommentImage
import com.m.vodovoz.feature.product_comments.model.CommentMediaUi

private val CommentPhotoWidth = 48.dp

@Suppress("NonSkippableComposable")
@Composable
fun ProductDetailsComments(
    modifier: Modifier = Modifier,
    commentsCount: Int,
    comments: List<CommentUi>,
    commentPhotos: List<CommentMediaUi.Image>,
    onMediaClick: (CommentMediaUi, List<CommentMediaUi>) -> Unit,
    onWriteCommentClick: () -> Unit,
    onShowAllCommentsClick: () -> Unit,
) {
    Column(modifier = modifier) {

        TitleAndButton(
            title = stringResource(id = R.string.comments),
            button = if (commentsCount > 0) ButtonUi.Empty else null,
            onShowAllClick = { onShowAllCommentsClick() })


        if (commentPhotos.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.padding(top = 12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(commentPhotos, key = { it.url }) { photo ->
                    CommentImage(
                        imageWidth = CommentPhotoWidth,
                        imageHeight = CommentPhotoWidth * 1.5f,
                        media = photo,
                        onClick = { onMediaClick(it, commentPhotos) }
                    )
                }
            }
        }

        if (commentsCount > 0) {
            CommentsPager(
                modifier = Modifier.padding(top = if (commentPhotos.isEmpty()) 24.dp else 12.dp),
                comments = comments,
                onMediaClick = onMediaClick
            )
        } else {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 24.dp)
                    .padding(horizontal = 32.dp),
                text = stringResource(R.string.no_comments),
                color = MaterialTheme.colorScheme.surfaceTint,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }

        VodovozButton(
            modifier = Modifier
                .padding(top = 18.dp)
                .padding(horizontal = 16.dp),
            text = stringResource(R.string.leave_rate),
            onClick = onWriteCommentClick,
            colors = VodovozButtonDefaults.secondaryColors()
        )


    }
}

@Suppress("NonSkippableComposable")
@Composable
private fun CommentsPager(
    modifier: Modifier = Modifier,
    comments: List<CommentUi>,
    onMediaClick: (CommentMediaUi, List<CommentMediaUi>) -> Unit,
) {
    val pagerState = rememberPagerState { comments.size }

    HorizontalPager(
        modifier = modifier,
        state = pagerState,
        contentPadding = PaddingValues(horizontal = 16.dp),
        pageSpacing = 8.dp,
        beyondViewportPageCount = comments.size,
        verticalAlignment = Alignment.Top
    ) { page ->
        val comment = comments[page]
        CommentCard(
            comment = comment,
            minLines = 2,
            maxLines = 2,
            mediaThumbnailWidth = CommentPhotoWidth,
            onMediaClick = { onMediaClick(it, comment.media) }
        )
    }
}
