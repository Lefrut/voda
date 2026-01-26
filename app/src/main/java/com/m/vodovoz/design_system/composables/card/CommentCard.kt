package com.m.vodovoz.design_system.composables.card

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.video.videoFrameMillis
import coil3.video.videoFramePercent
import com.m.vodovoz.R
import com.m.vodovoz.design_system.model.CommentUi
import com.m.vodovoz.feature.product_comments.model.CommentImage
import com.m.vodovoz.feature.product_comments.model.CommentMediaUi

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CommentCard(
    modifier: Modifier = Modifier,
    comment: CommentUi,
    minLines: Int = 2,
    maxLines: Int = Int.MAX_VALUE,
    sharedTransitionScope: SharedTransitionScope? = null,
    sharedElementsIsVisible: Boolean = true,
    onMediaClick: (CommentMediaUi) -> Unit = {},
) {
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth(),
        elevation = CardDefaults.outlinedCardElevation(0.dp),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.background),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = comment.userName,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.width(4.dp))

                (1..5).forEach { starNumber ->
                    Icon(
                        painter = painterResource(id = R.drawable.ic_star_active_v2),
                        contentDescription = null,
                        tint = if (starNumber <= comment.rating) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (comment.text.isNotEmpty()) {
                Text(
                    modifier = Modifier.padding(top = 12.dp),
                    text = AnnotatedString.fromHtml(comment.text),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = maxLines,
                    minLines = minLines
                )
            }

            if (comment.media.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    comment.media.forEach { media ->

                        key(media) {
                            CommentImage(
                                imageWidth = 60.dp,
                                imageHeight = 90.dp,
                                media = media,
                                sharedTransitionScope = null,
                                playIconSize = 12.dp,
                                onClick = {
                                    onMediaClick(media)
                                }
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comment.dateText,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.surfaceTint,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}