package com.vodovoz.app.feature.product_details.detail_media

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.vodovoz.app.R
import com.vodovoz.app.design_system.model.ProductMediaUi
import com.vodovoz.app.feature.product_details.detail_media.composable.DetailMediaImage
import com.vodovoz.app.feature.product_details.detail_media.composable.DetailMediaRutubeVideo
import com.vodovoz.app.feature.product_details.detail_media.composable.DetailMediaYoutubeVideo
import com.vodovoz.app.feature.product_details.detail_media.model.DetailMediaState
import mx.platacard.pagerindicator.PagerIndicatorOrientation
import mx.platacard.pagerindicator.PagerWormIndicator

@Composable
fun DetailMediaScreen(
    viewModel: DetailMediaViewModel,
    viewState: DetailMediaState,
    pagerState: PagerState,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.icon_close),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.End)
                .clip(CircleShape)
                .clickable { viewModel.navigateBack() }
                .padding(16.dp)
                .size(24.dp),
            tint = MaterialTheme.colorScheme.onBackground
        )




        Spacer(modifier = Modifier.weight(0.4f))

        val mediaList = viewState.mediaList

        HorizontalPager(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            state = pagerState,
            key = { i -> mediaList.getOrElse(i) { i } },
            pageSpacing = 2.dp,
            beyondViewportPageCount = 1,
            userScrollEnabled = true
        ) { pageIndex ->
            val media = mediaList.getOrNull(pageIndex) ?: return@HorizontalPager

            val videoModifier = when (Configuration.ORIENTATION_PORTRAIT) {
                Configuration.ORIENTATION_PORTRAIT -> {
                    Modifier
                        .height(400.dp)
                        .fillMaxWidth()
                }

                else -> {
                    Modifier.fillMaxSize()
                }
            }

            when (media) {
                is ProductMediaUi.Picture -> {
                    DetailMediaImage(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        image = media.url
                    )
                }

                is ProductMediaUi.YoutubeVideo -> {
                    DetailMediaRutubeVideo(
                        modifier = videoModifier,
                        videoCode = media.code,
                        onPortrait = {
                            viewModel.makePortrait()
                        },
                        onLandscape = {
                            viewModel.makeLandscape()
                        }
                    )
                }

                is ProductMediaUi.RutubeVideo -> {
                    DetailMediaRutubeVideo(
                        modifier = videoModifier,
                        videoCode = media.code,
                        onPortrait = {
                            viewModel.makePortrait()
                        },
                        onLandscape = {
                            viewModel.makeLandscape()
                        }
                    )
                }
            }

        }

        Spacer(modifier = Modifier.weight(0.7f))

        PagerWormIndicator(
            modifier = Modifier
                .padding(bottom = 45.dp)
                .height(5.dp)
                .graphicsLayer {
                    alpha = if (mediaList.size > 1) 1f else 0f
                },
            pagerState = pagerState,
            activeDotColor = MaterialTheme.colorScheme.primary,
            dotColor = MaterialTheme.colorScheme.surfaceVariant,
            dotCount = mediaList.size,
            orientation = PagerIndicatorOrientation.Horizontal,
            minDotSize = 5.dp,
            activeDotSize = 5.dp,
            space = 6.dp
        )

    }
}