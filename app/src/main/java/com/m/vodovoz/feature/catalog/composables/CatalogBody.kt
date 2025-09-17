package com.m.vodovoz.feature.catalog.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.m.vodovoz.design_system.composables.decoration.AdvertisingChip
import com.m.vodovoz.design_system.composables.decoration.LocalShimmer
import com.m.vodovoz.design_system.composables.decoration.SkeletonBox
import com.m.vodovoz.design_system.model.AboutAdvertisingUi
import com.m.vodovoz.design_system.model.BannerUi
import com.m.vodovoz.design_system.model.ParentCategoryUi
import com.m.vodovoz.feature.home.composables.AutoScrollImagePager
import com.m.vodovoz.feature.home.composables.rememberAutoScrollPagerState

@Suppress("NonSkippableComposable")
@Composable
fun CatalogBody(
    modifier: Modifier = Modifier,
    categories: List<ParentCategoryUi>,
    banners: List<BannerUi>,
    onBannerClick: (BannerUi) -> Unit,
    onCategoryClick: (ParentCategoryUi) -> Unit,
    onAboutAdvertisingClick: (AboutAdvertisingUi) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        val pictures = banners.map { it.detailPicture }
        val pagerState = rememberAutoScrollPagerState(itemsCount = pictures.size)

        if (banners.size > 1) {
            AutoScrollImagePager(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .height(68.dp),
                images = pictures,
                onImageClick = { page ->
                    onBannerClick(banners[page])
                },
                pagerState = pagerState,
                pageWidth = Dp.Unspecified,
                chip = { page ->
                    val advertising = banners[page].advertising
                    advertising?.let {
                        AdvertisingChip { onAboutAdvertisingClick(advertising) }
                    }
                }
            )
        }


        val cardModifier = Modifier.weight(1f)
        CompositionLocalProvider(value = LocalShimmer provides rememberShimmer(shimmerBounds = ShimmerBounds.View)) {
            FlowRow(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 2
            ) {
                categories.forEach { category ->
                    CatalogCard(
                        modifier = cardModifier,
                        title = category.name,
                        image = category.picture,
                        onClick = {
                            onCategoryClick(category)
                        }
                    )
                }

                if (categories.size % 2 == 1) {
                    Spacer(modifier = cardModifier)
                }
            }
        }
    }
}

@Composable
private fun CatalogCard(
    title: String,
    image: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    SkeletonBox(
        modifier = modifier
            .height(140.dp)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface.copy(0.7f))
            .clickable { onClick() },
        shimmerState = LocalShimmer.current
    ) {
        AsyncImage(
            model = image,
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds,
        )
        Text(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}