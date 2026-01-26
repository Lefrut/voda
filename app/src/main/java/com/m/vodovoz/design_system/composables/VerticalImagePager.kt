package com.m.vodovoz.design_system.composables

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.m.vodovoz.R
import com.m.vodovoz.design_system.composables.zoom.ZoomableContainer
import com.m.vodovoz.design_system.composables.zoom.rememberZoomableState
import com.m.vodovoz.feature.product_comments.model.CommentMediaUi

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun VerticalImagePager(
    modifier: Modifier = Modifier,
    initialPage: Int,
    images: List<CommentMediaUi.Image>,
    sharedTransitionScope: SharedTransitionScope,
    onCloseClick: () -> Unit,
) {
    val pagerState = rememberPagerState(initialPage) { images.size }

    VerticalPager(
        modifier = modifier,
        state = pagerState,
        beyondViewportPageCount = 1,
        key = { page -> images.getOrNull(page)?.url ?: page }
    ) { page ->

        val image = images.getOrElse(page) { CommentMediaUi.Image("") }.url

        val asyncImagePainter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(image)
                .memoryCacheKey(image)
                .placeholderMemoryCacheKey(image)
                .build(),
        )
        val zoomableState =
            rememberZoomableState(
                contentSize = asyncImagePainter.intrinsicSize,
                maxScale = 3f,
                animationSpec = tween(1000, easing = LinearEasing),
            )


        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            val isVerticalImage =
                asyncImagePainter.intrinsicSize.height > asyncImagePainter.intrinsicSize.width

            sharedTransitionScope.apply {
                ZoomableContainer(
                    modifier = Modifier.fillMaxSize(),
                    state = zoomableState,
                    boundClip = false
                ) {
                    Image(
                        modifier = Modifier
                            .then(
                                if (isVerticalImage) {
                                    Modifier
                                        .wrapContentWidth(unbounded = true)
                                        .wrapContentHeight(unbounded = true)
                                        .height(
                                            with(LocalDensity.current) {
                                                zoomableState.containerHeight.toDp()
                                            }
                                        )
                                } else Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 300.dp)
                            )
                            .clip(MaterialTheme.shapes.small),
                        painter = asyncImagePainter,
                        contentDescription = null,
                        alignment = Alignment.Center
                    )

                }
            }

            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_close),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(
                        end = 16.dp,
                        top = 8.dp
                    )
                    .clip(CircleShape)
                    .clickable(onClick = onCloseClick)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp)
                    .size(24.dp)
                    .zIndex(Float.MAX_VALUE),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }

}