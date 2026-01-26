package com.m.vodovoz.feature.product_comments.model

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.SizeResolver
import com.m.vodovoz.R
import com.m.vodovoz.design_system.composables.decoration.LocalShimmer
import com.m.vodovoz.design_system.composables.decoration.SkeletonBox
import com.m.vodovoz.domain.general.model.product.CommentMediaModel
import com.m.vodovoz.domain.general.model.product.ProductCommentsInfoModel
import com.m.vodovoz.domain.general.model.product.SortModel
import com.m.vodovoz.ui.compose.player.rememberExoVideoFrame
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer


@Immutable
data class ProductCommentsInfoUi(
    val sorting: List<SortUi>,
    val ratingText: String,
    val commentsCount: Int,
    val commentsCountText: String,
    val images: List<String>,
    val media: List<CommentMediaUi>
) {
    companion object {
        val Empty = ProductCommentsInfoUi(
            sorting = emptyList(),
            ratingText = "",
            commentsCount = 0,
            commentsCountText = "",
            images = emptyList(),
            media = emptyList()
        )
    }
}

@Stable
sealed class CommentMediaUi {

    abstract val url: String


    data class Video(override val url: String) : CommentMediaUi()

    data class Image(override val url: String) : CommentMediaUi()
}


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CommentImage(
    modifier: Modifier = Modifier,
    imageWidth: Dp,
    imageHeight: Dp,
    media: CommentMediaUi,
    sharedTransitionScope: SharedTransitionScope? = null,
    transitionKey: String = media.url,
    onClick: (CommentMediaUi) -> Unit
) {
    val context = LocalContext.current
    var isLoading by remember(media.url) { mutableStateOf(true) }
    val mediaUrl = media.url


    LaunchedEffect(Unit) {
        when (media) {
            is CommentMediaUi.Image -> {
                context.imageLoader.execute(
                    ImageRequest.Builder(context)
                        .data(mediaUrl)
                        .size(SizeResolver.ORIGINAL)
                        .crossfade(false)
                        .placeholderMemoryCacheKey(transitionKey)
                        .memoryCacheKey(transitionKey)
                        .build()
                )
            }

            is CommentMediaUi.Video -> {

            }
        }
    }


    Box(modifier = modifier) {

        val mediaData by rememberExoVideoFrame(
            url = mediaUrl,
            frameMs = 1000L,
            width = imageWidth
        )


        AsyncImage(
            model = when (media) {
                is CommentMediaUi.Image -> ImageRequest.Builder(context)
                    .data(media.url)
                    .crossfade(true)
                    .build()

                is CommentMediaUi.Video -> mediaData
            },
            contentDescription = null,
            modifier = Modifier
                .height(imageHeight)
                .width(imageWidth)
                .then(
                    sharedTransitionScope?.run {
                        Modifier.sharedElementWithCallerManagedVisibility(
                            sharedContentState = rememberSharedContentState(
                                key = transitionKey
                            ),
                            visible = true,
                            renderInOverlayDuringTransition = false
                        )
                    } ?: Modifier
                )
                .clip(MaterialTheme.shapes.small)
                .clickable {
                    onClick(media)
                },
            contentScale = ContentScale.Crop,
            onLoading = {
                isLoading = true
            },
            onSuccess = {
                isLoading = false
            },
            onError = {
                isLoading = false
            },
        )

        val videoIsLoading = mediaData == null && media is CommentMediaUi.Video



        if (isLoading || videoIsLoading) {
            SkeletonBox(
                shimmerState = LocalShimmer.current,
                modifier = Modifier
                    .matchParentSize()
                    .clip(MaterialTheme.shapes.small)
            )
        } else if (media is CommentMediaUi.Video) {
            Image(
                painter = painterResource(R.drawable.svg_play_video),
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.Center),
                contentDescription = null,
            )
        }
    }

}


fun ProductCommentsInfoModel.toUi(): ProductCommentsInfoUi {
    return ProductCommentsInfoUi(
        sorting = sorting.map { it.toUi() },
        ratingText = ratingText,
        commentsCount = commentsCount,
        commentsCountText = commentsCountText,
        images = images,
        media = media.mapToUi()
    )
}

@JvmName("mapToCommentMediaUiList")
fun List<CommentMediaModel>.mapToUi(): List<CommentMediaUi> {
    return map { it.toUi() }
}

fun CommentMediaModel.toUi(): CommentMediaUi {
    return when (this) {
        is CommentMediaModel.Image -> CommentMediaUi.Image(url)
        is CommentMediaModel.Video -> CommentMediaUi.Video(url)
    }
}

@JvmName("mapToComentMediaModelList")
fun List<CommentMediaUi>.mapToDomain(): List<CommentMediaModel> {
    return map { it.toDomain() }
}

fun CommentMediaUi.toDomain(): CommentMediaModel {
    return when (this) {
        is CommentMediaUi.Image -> CommentMediaModel.Image(url)
        is CommentMediaUi.Video -> CommentMediaModel.Video(url)
    }
}


fun ProductCommentsInfoUi.toDomain(): ProductCommentsInfoModel {
    return ProductCommentsInfoModel(
        sorting = sorting.map { sort -> sort.toDomain() },
        ratingText = ratingText,
        commentsCount = commentsCount,
        commentsCountText = commentsCountText,
        images = images,
        media = media.mapToDomain()
    )
}

@Immutable
data class SortUi(
    val name: String,
    val value: String,
    val order: String,
) {
    companion object {
        val Empty = SortUi("", "", "")
    }
}

fun SortModel.toUi(): SortUi {
    return SortUi(
        name = name,
        value = value,
        order = order
    )
}

@JvmName("mapToSortUiList")
fun List<SortModel>.mapToUi(): List<SortUi> {
    return map { it.toUi() }
}

fun SortUi.toDomain(): SortModel {
    return SortModel(
        name = name,
        value = value,
        order = order
    )
}