package com.m.vodovoz.feature.stories_fragment

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import coil3.compose.AsyncImage
import coil3.imageLoader
import coil3.memory.MemoryCache
import coil3.request.ImageRequest
import com.m.vodovoz.R
import com.m.vodovoz.design_system.composables.button.VodovozButton
import com.m.vodovoz.feature.stories_fragment.composables.StoriesIndicator
import kotlin.math.absoluteValue


@SuppressLint("RestrictedApi")
@Composable
fun StoriesScreen(
    viewState: StoriesViewModel.HistoriesSliderState,
    viewModel: StoriesViewModel,
    pagerState: PagerState,
) {
    val stories = viewState.stories
    val context = LocalContext.current

    LaunchedEffect(viewState.currentStoryIndex) {
        stories.getOrNull(viewState.currentStoryIndex)?.pages?.forEach { storyPage ->
            val key = MemoryCache.Key(storyPage.image)

            val alreadyInMemory = context.imageLoader.memoryCache?.get(key) != null
            if (alreadyInMemory) return@forEach

            val request = ImageRequest.Builder(context)
                .data(storyPage.image)
                .memoryCacheKey(key)
                .build()

            context.imageLoader.execute(request)
        }
    }

    HorizontalPager(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .pointerInput(Unit) {
                handleStoryTaps(
                    onTapStart = { viewModel.stopStory() },
                    onPrev = { viewModel.goPreviousStoryPage() },
                    onNext = { viewModel.goNextStoryPage() },
                    onTapEnd = { viewModel.resumeStory() }
                )
            },
        state = pagerState,
        beyondViewportPageCount = stories.size,
        key = { page ->
            val currentStory = stories.getOrNull(page)
            currentStory?.id ?: -kotlin.random.Random.nextInt()
        }
    ) { i ->
        val story = stories.getOrNull(i) ?: return@HorizontalPager

        val storyPage =
            story.pages.getOrNull(viewState.currentPageIndex) ?: story.pages.firstOrNull()
            ?: return@HorizontalPager

        Box(
            modifier = Modifier
                .padding(vertical = 10.dp)
                .graphicsLayer {
                    val pageOffset = (
                            (pagerState.currentPage - i) + pagerState
                                .currentPageOffsetFraction
                            ).absoluteValue


                    val scale = lerp(
                        start = 0.65f,
                        stop = 1f,
                        1f - pageOffset.coerceIn(0f, 1f)
                    )

                    val alphaValue = lerp(
                        start = 0.2f,
                        stop = 1f,
                        1f - pageOffset.coerceIn(0f, 1f)
                    )

                    alpha = alphaValue
                    scaleX = scale
                    scaleY = scale
                }
                .clip(MaterialTheme.shapes.large)
        ) {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = if (viewState.currentStoryIndex != i) story.pages.firstOrNull()?.image
                    ?: "" else storyPage.image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center
            )

            val timePassed = rememberUpdatedState(newValue = viewState.timePassed.toFloat())
            val storyDuration = rememberUpdatedState(newValue = storyPage.durationMillis)

            val actionWithButton = storyPage.actionWithButton

            StoryDecorations(
                countPages = story.pages.size,
                pageIndex = if (viewState.currentStoryIndex != i) 0 else viewState.currentPageIndex,
                pageProgress = {
                    if (viewState.currentStoryIndex != i) 0f
                    else timePassed.value / storyDuration.value
                },
                onCloseClick = {
                    viewModel.navigateBack()
                },
            ) {
                val colorfulButton = actionWithButton.colorfulButton

                VodovozButton(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
                    text = colorfulButton.name,
                    onClick = {
                        viewModel.activateButtonAction(actionWithButton.action)
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = colorfulButton.backgroundColor,
                        contentColor = colorfulButton.textColor
                    )
                )
            }

        }

    }
}

@Composable
private fun StoryDecorations(
    modifier: Modifier = Modifier,
    countPages: Int,
    pageIndex: Int,
    pageProgress: () -> Float,
    onCloseClick: () -> Unit,
    bottomButton: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        StoriesIndicator(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .padding(top = 8.dp),
            countPages = countPages,
            pageIndex = pageIndex,
            progress = pageProgress
        )
        CloseButton(
            modifier = Modifier
                .align(Alignment.End)
                .padding(16.dp),
            onCloseClick = onCloseClick
        )
        Spacer(modifier = Modifier.weight(1f))

        bottomButton()
    }

}


@Composable
private fun CloseButton(modifier: Modifier = Modifier, onCloseClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .size(32.dp)
            .clickable { onCloseClick() }
            .background(MaterialTheme.colorScheme.background.copy(0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_close_stories),
            contentDescription = null,
            modifier = Modifier.size(13.dp),
            tint = MaterialTheme.colorScheme.background
        )
    }
}

private suspend fun PointerInputScope.handleStoryTaps(
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onTapEnd: () -> Unit,
    onTapStart: () -> Unit,
    tapTimeout: Long = 160,
    moveThreshold: Float = 10f,
) {
    awaitPointerEventScope {
        while (true) {
            val down = awaitFirstDown()
            onTapStart()
            val start = down.position
            val startTime = System.currentTimeMillis()
            val up = waitForUpOrCancellation()
            val duration = System.currentTimeMillis() - startTime

            if (up != null && (up.position - start).getDistance() < moveThreshold && duration < tapTimeout) {
                val x = up.position.x
                val width = size.width.toFloat()
                when {
                    x < width / 2.5f -> onPrev()
                    x > width - width / 2.5f -> onNext()
                }
            }

            onTapEnd()
        }
    }
}