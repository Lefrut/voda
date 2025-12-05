package com.m.vodovoz.design_system.composables.decoration

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.m.vodovoz.design_system.model.AboutAdvertisingUi
import com.m.vodovoz.design_system.model.BannerUi
import com.m.vodovoz.feature.home.composables.AutoScrollImagePager
import com.m.vodovoz.feature.home.composables.rememberAutoScrollPagerState

@Composable
fun SmallBannerPager(
    modifier: Modifier = Modifier,
    banners: List<BannerUi>,
    onBannerClick: (BannerUi) -> Unit,
    onAboutAdvertisingClick: (AboutAdvertisingUi) -> Unit,
) {
    val pagerState = rememberAutoScrollPagerState(itemsCount = banners.size)
    val bannerImages = banners.map { it.detailPicture }
    if (banners.isNotEmpty()) {
        AutoScrollImagePager(
            modifier = modifier
                .fillMaxWidth()
                .height(68.dp),
            images = bannerImages,
            onImageClick = { page ->
                banners.getOrNull(page)?.let { banner ->
                    onBannerClick(banner)
                }
            },
            pagerState = pagerState,
            pageWidth = Dp.Unspecified,
            chip = { page ->
                val advertising = banners.getOrNull(page)?.advertising
                advertising?.let {
                    AdvertisingChip { onAboutAdvertisingClick(advertising) }
                }
            }
        )
    }

}