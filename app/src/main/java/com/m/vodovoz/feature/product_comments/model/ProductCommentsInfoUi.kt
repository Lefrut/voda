package com.m.vodovoz.feature.product_comments.model

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.OverlayClip
import androidx.compose.animation.SharedTransitionScope.SharedContentState
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.m.vodovoz.R
import com.m.vodovoz.design_system.composables.decoration.LocalShimmer
import com.m.vodovoz.design_system.composables.decoration.SkeletonBox
import com.m.vodovoz.domain.general.model.product.CommentMediaModel
import com.m.vodovoz.domain.general.model.product.ProductCommentsInfoModel
import com.m.vodovoz.domain.general.model.product.SortModel
import com.m.vodovoz.ui.compose.player.rememberExoVideoFrame


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

    @Immutable
    data class Video(override val url: String) : CommentMediaUi()

    @Immutable
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
    animatedContentScope: AnimatedContentScope? = null,
    transitionKey: String = media.url,
    playIconSize: Dp = 16.dp,
    onClick: (CommentMediaUi) -> Unit
) {
    val context = LocalContext.current
    var imageIsLoading by remember(media.url) { mutableStateOf(true) }
    val mediaUrl = media.url


    val sharedElementModifier = if (sharedTransitionScope != null && animatedContentScope != null) {
        with(sharedTransitionScope) {
            Modifier.sharedElement(
                sharedContentState = rememberSharedContentState(transitionKey),
                animatedVisibilityScope = animatedContentScope,
            )
        }
    } else {
        Modifier
    }


    Box(modifier = modifier.clip(MaterialTheme.shapes.small)) {

        val mediaData by rememberExoVideoFrame(
            url = mediaUrl,
            frameMs = 1000L,
            width = imageWidth
        )

        val videoIsLoading = mediaData == null && media is CommentMediaUi.Video


        AsyncImage(
            model = when (media) {
                is CommentMediaUi.Image -> ImageRequest.Builder(context)
                    .data(mediaUrl)
                    .memoryCacheKey(mediaUrl)
                    .placeholderMemoryCacheKey(mediaUrl)
                    .crossfade(true)
                    .build()

                is CommentMediaUi.Video -> mediaData
            },
            contentDescription = null,
            modifier = Modifier
                .height(imageHeight)
                .width(imageWidth)
                .then(sharedElementModifier)
                .clip(MaterialTheme.shapes.small)
                .clickable {
                    if (!imageIsLoading && !videoIsLoading) {
                        onClick(media)
                    }
                },
            contentScale = ContentScale.Crop,
            onLoading = {
                imageIsLoading = true
            },
            onSuccess = {
                imageIsLoading = false
            },
            onError = {
                imageIsLoading = false
            },
        )


        if (imageIsLoading || videoIsLoading) {
            SkeletonBox(
                shimmerState = LocalShimmer.current,
                modifier = Modifier.matchParentSize()
            )
        } else if (media is CommentMediaUi.Video) {
            Image(
                painter = painterResource(R.drawable.svg_play_video),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .size(playIconSize),
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