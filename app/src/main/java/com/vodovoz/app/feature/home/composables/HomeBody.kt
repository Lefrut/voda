package com.vodovoz.app.feature.home.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vodovoz.app.common.model.ButtonAction
import com.vodovoz.app.design_system.composables.decoration.VodovozHorizontalDivider
import com.vodovoz.app.design_system.model.AboutAdvertisingUi
import com.vodovoz.app.design_system.model.BannerUi
import com.vodovoz.app.design_system.model.ProductUi
import com.vodovoz.app.design_system.model.PromotionUi
import com.vodovoz.app.design_system.model.StoryUi
import com.vodovoz.app.feature.home.model.HomeListItem
import com.vodovoz.app.feature.home.model.HomeOrderUi
import com.vodovoz.app.feature.home.model.MenuItemUi
import com.vodovoz.app.feature.home.model.PopularCategoryUi

@Suppress("NonSkippableComposable")
@Composable
fun HomeBody(
    modifier: Modifier = Modifier,
    items: List<HomeListItem>,
    onStoryClick: (StoryUi) -> Unit,
    onCategorySelect: (HomeListItem.Products.CategoryWithProductsSection, Long) -> Unit,
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

        items.sortedBy { item -> item.position }.forEach { item ->
            key(item.position) {
                when (item) {
                    is HomeListItem.Banner -> {
                        val banners = item.value
                        if (banners.isNotEmpty()) {
                            HomeBanners(
                                banners = banners,
                                onAdvertisingClick = onAboutAdvertisingClick,
                                onBannerClick = onBannerClick
                            )
                        }
                    }

                    is HomeListItem.OrderWithMenu -> {
                        val orderWithMenu = item.value
                        if (orderWithMenu.order != null || orderWithMenu.menuItems.isNotEmpty()) {
                            HomeOrderMenu(
                                modifier = Modifier.padding(top = 24.dp),
                                orderWithMenu = orderWithMenu,
                                onOrderClick = onOrderClick,
                                onMenuItemClick = onOrderMenuItemClick
                            )
                        }
                    }

                    is HomeListItem.PopularCategories -> {
                        val sectionPopularCategories = item.value
                        if (sectionPopularCategories.items.isNotEmpty()) {
                            HomePopularCategories(
                                modifier = Modifier.padding(top = 4.dp),
                                onPopularCategoryClick = onPopularCategoryClick,
                                sectionPopularCategories = sectionPopularCategories
                            )
                        }
                    }

                    is HomeListItem.Products.CategoryWithProductsSection -> {
                        val categorySection = item.item

                        if(categorySection.items.isNotEmpty()){
                            HomeCategoryProducts(
                                modifier = Modifier.padding(top = 32.dp),
                                onShowAllClick = onShowAllClick,
                                categoryWithProductsId = item.currentCategoryId,
                                sectionCategoriesWithProducts = categorySection,
                                onCategorySelect = { categoryWithProducts ->
                                    onCategorySelect(item, categoryWithProducts.id)
                                },
                                onProductClick = onProductCardClick,
                                onProductLike = onProductLike,
                                onIncrementToCart = onIncrementProductToCart,
                                onDecrementToCart = onDecrementProductToCart,
                                onProductAnalogsClick = onProductAnalogsClick
                            )
                        }
                    }

                    is HomeListItem.Products.Section -> {
                        val productsSection = item.item

                        if (productsSection.items.isNotEmpty()) {
                            HomeProductsRow(
                                modifier = Modifier.padding(top = 32.dp),
                                sectionProducts = productsSection,
                                onProductClick = onProductCardClick,
                                onShowAllClick = onShowAllClick,
                                onProductLike = onProductLike,
                                onDecrementToCart = onDecrementProductToCart,
                                onIncrementToCart = onIncrementProductToCart,
                                onAnalogsClick = onProductAnalogsClick
                            )

                        }
                    }

                    is HomeListItem.Promotions -> {
                        val sectionPromotions = item.value
                        if (sectionPromotions.items.isNotEmpty()) {
                            HomePromotions(
                                modifier = Modifier.padding(top = 32.dp),
                                onShowAllClick = onShowAllClick,
                                onPromotionClick = onPromotionClick,
                                sectionPromotions = sectionPromotions,
                                onAboutAdvertisingClick = onAboutAdvertisingClick
                            )
                        }
                    }

                    is HomeListItem.Stories -> {
                        val stories = item.value
                        if (stories.isNotEmpty()) {
                            HomeStories(
                                modifier = Modifier.padding(top = 16.dp),
                                stories = stories,
                                onStoryClick = onStoryClick
                            )
                        }
                    }

                    is HomeListItem.Divider -> {
                        VodovozHorizontalDivider(modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

    }
}