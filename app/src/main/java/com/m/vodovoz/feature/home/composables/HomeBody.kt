package com.m.vodovoz.feature.home.composables

import android.app.Application
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.EventListener
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import coil3.video.VideoFrameDecoder
import com.m.vodovoz.common.model.ButtonAction
import com.m.vodovoz.core.analytics.Analytics
import com.m.vodovoz.design_system.composables.decoration.VodovozHorizontalDivider
import com.m.vodovoz.design_system.model.AboutAdvertisingUi
import com.m.vodovoz.design_system.model.BannerUi
import com.m.vodovoz.design_system.model.CategoryWithProductsUi
import com.m.vodovoz.design_system.model.ProductUi
import com.m.vodovoz.design_system.model.PromotionUi
import com.m.vodovoz.design_system.model.StoryUi
import com.m.vodovoz.feature.home.model.HomeItem
import com.m.vodovoz.feature.home.model.HomeListItem
import com.m.vodovoz.feature.home.model.HomeOrderUi
import com.m.vodovoz.feature.home.model.MenuItemUi
import com.m.vodovoz.feature.home.model.PopularCategoryUi


@Composable
fun HomeBody(
    modifier: Modifier = Modifier,
    items: List<HomeListItem<*>>,
    onStoryClick: (StoryUi) -> Unit,
    onCategorySelect: (HomeListItem.Products.CategoriesWithProductsSection, CategoryWithProductsUi) -> Unit,
    onPopularCategoryClick: (PopularCategoryUi) -> Unit,
    onOrderClick: (HomeOrderUi) -> Unit,
    onOrderMenuItemClick: (MenuItemUi) -> Unit,
    onShowAllClick: (ButtonAction) -> Unit,
    onProductCardClick: (ProductUi) -> Unit,
    onProductLike: (ProductUi) -> Unit,
    onPromotionClick: (PromotionUi) -> Unit,
    onAboutAdvertisingClick: (AboutAdvertisingUi) -> Unit,
    onBannerClick: (BannerUi) -> Unit,
    onIncrementProductToCart: (ProductUi) -> Unit,
    onDecrementProductToCart: (ProductUi) -> Unit,
    onProductAnalogsClick: (ProductUi) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState(),
                rememberOverscrollEffect()
            )
    ) {
        items.sortedBy { it.position }.forEach { item ->
            key(item.position) {
                HomeItem(
                    item = item,
                    banner = {
                        HomeBanners(
                            banners = value,
                            onAdvertisingClick = onAboutAdvertisingClick,
                            onBannerClick = onBannerClick
                        )
                    },
                    divider = {
                        VodovozHorizontalDivider(modifier = Modifier.padding(top = 4.dp))
                    },
                    orderWithMenu = {
                        HomeOrderMenu(
                            modifier = Modifier.padding(top = 24.dp),
                            orderWithMenu = value,
                            onOrderClick = onOrderClick,
                            onMenuItemClick = onOrderMenuItemClick
                        )
                    },
                    popularCategories = {
                        HomePopularCategories(
                            modifier = Modifier.padding(top = 4.dp),
                            onPopularCategoryClick = onPopularCategoryClick,
                            sectionPopularCategories = value
                        )
                    },
                    categoriesWithProductsSection = {
                        HomeCategoryProducts(
                            modifier = Modifier.padding(top = 32.dp),
                            onShowAllClick = onShowAllClick,
                            categoryWithProductsId = currentCategoryId,
                            sectionCategoriesWithProducts = value,
                            onCategorySelect = { categoryWithProducts ->
                                onCategorySelect(this, categoryWithProducts)
                            },
                            onProductClick = onProductCardClick,
                            onProductLike = onProductLike,
                            onIncrementToCart = onIncrementProductToCart,
                            onDecrementToCart = onDecrementProductToCart,
                            onProductAnalogsClick = onProductAnalogsClick
                        )
                    },
                    productsSection = {
                        ProductSectionRow(
                            modifier = Modifier.padding(top = 32.dp),
                            sectionProducts = value,
                            onProductClick = {
                                if (position == HomeListItem.Positions.NEW_PRODUCTS) {
                                    Analytics.reportEvent("new_product_tap")
                                }
                                onProductCardClick(it)
                            },
                            onShowAllClick = {
                                if (position == HomeListItem.Positions.NEW_PRODUCTS) {
                                    Analytics.reportEvent("new_section_see_all_tap")
                                }
                                onShowAllClick(it)

                            },
                            onProductLike = onProductLike,
                            onDecrementToCart = onDecrementProductToCart,
                            onIncrementToCart = onIncrementProductToCart,
                            onAnalogsClick = onProductAnalogsClick
                        )
                    },
                    promotions = {
                        HomePromotions(
                            modifier = Modifier.padding(top = 32.dp),
                            onShowAllClick = onShowAllClick,
                            onPromotionClick = onPromotionClick,
                            sectionPromotions = value,
                            onAboutAdvertisingClick = onAboutAdvertisingClick
                        )
                    },
                    stories = {
                        HomeStories(
                            modifier = Modifier.padding(top = 16.dp),
                            stories = value,
                            onStoryClick = onStoryClick
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

    }
}