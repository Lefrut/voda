package com.vodovoz.app.feature.stories_fragment

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
import com.vodovoz.app.R
import com.vodovoz.app.design_system.composables.button.VodovozButton
import com.vodovoz.app.feature.stories_fragment.composables.StoriesIndicator
import kotlinx.coroutines.delay
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
        delay(75L)
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
            .background(MaterialTheme.colorScheme.onBackground)
            .windowInsetsPadding(WindowInsets.systemBars)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val change = awaitFirstDown()
                        val startTime = System.currentTimeMillis()
                        viewModel.stopStory()
                        waitForUpOrCancellation()
                        if (System.currentTimeMillis() - startTime < 160) {
                            if (change.position.x < size.width / 2.5) {
                                viewModel.goPreviousStoryPage()
                            } else if (change.position.x > size.width - (size.width / 2.5)) {
                                viewModel.goNextStoryPage()
                            }
                        }
                        viewModel.resumeStory()

                    }
                }
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                StoriesIndicator(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .padding(top = 8.dp),
                    countPages = story.pages.size,
                    pageIndex = if (viewState.currentStoryIndex != i) 0 else viewState.currentPageIndex,
                    progress = {
                        if (viewState.currentStoryIndex != i) 0f
                        else timePassed.value / storyDuration.value
                    }
                )
                CloseButton(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(16.dp),
                    onCloseClick = {
                        viewModel.navigateBack()
                    }
                )
                Spacer(modifier = Modifier.weight(1f))

                val actionWithButton = storyPage.actionWithButton
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