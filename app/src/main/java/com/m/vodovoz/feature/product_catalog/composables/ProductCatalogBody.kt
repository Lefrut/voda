package com.m.vodovoz.feature.product_catalog.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.m.vodovoz.design_system.composables.decoration.SmallBannerPager
import com.m.vodovoz.design_system.composables.list.ProductListCategoriesRow
import com.m.vodovoz.design_system.composables.list.ProductListOptionsRow
import com.m.vodovoz.design_system.composables.list.ProductListTitle
import com.m.vodovoz.design_system.composables.list.linearOrGridProducts
import com.m.vodovoz.design_system.composables.placeholders.VodovozPlaceholder
import com.m.vodovoz.design_system.model.AboutAdvertisingUi
import com.m.vodovoz.design_system.model.BannerUi
import com.m.vodovoz.design_system.model.ParentCategoryUi
import com.m.vodovoz.design_system.model.ProductUi
import com.m.vodovoz.design_system.model.allCategories
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.domain.general.model.exceptions.EmptyResultException
import com.m.vodovoz.feature.home.model.CategoryUi
import com.m.vodovoz.feature.product_comments.model.SortUi

@Suppress("NonSkippableComposable")
@Composable
fun ProductCatalogBody(
    modifier: Modifier = Modifier,
    lazyGridState: LazyGridState,
    banners: List<BannerUi>,
    title: String,
    categories: List<CategoryUi>,
    categoriesTree: List<ParentCategoryUi>,
    productsQuantity: String,
    currentCategory: CategoryUi,
    currentSort: SortUi,
    isGridView: Boolean,
    showShare: Boolean,
    showFilters: Boolean,
    showEmptyCategory: Boolean,
    products: List<ProductUi>,
    productsLoadStates: CombinedLoadStates,
    onProductSee: (Int) -> Unit,
    onSortingClick: () -> Unit,
    onBannerClick: (BannerUi) -> Unit,
    onAboutAdvertisingClick: (AboutAdvertisingUi) -> Unit,
    onSwitchLayoutClick: () -> Unit,
    onCategoryClick: (CategoryUi) -> Unit,
    onCategoriesListClick: () -> Unit,
    onProductClick: (ProductUi) -> Unit,
    onProductLike: (ProductUi) -> Unit,
    onFiltersClick: (() -> Unit)? = null,
    onShareClick: () -> Unit,
    onProductAnalogsClick: (ProductUi) -> Unit,
    onIncrementProductToCart: (ProductUi) -> Unit,
    onDecrementProductToCart: (ProductUi) -> Unit,
    onPlacholderButtonClick: () -> Unit
) {
    val shimmer = rememberShimmer(shimmerBounds = ShimmerBounds.View)

    val density = LocalDensity.current

    val placeholderHeight by remember(lazyGridState, density) {
        derivedStateOf {
            val li = lazyGridState.layoutInfo

            val viewportPx = (li.viewportEndOffset - li.viewportStartOffset)
                .takeIf { it > 0 } ?: li.viewportSize.height

            val headersPx = li.visibleItemsInfo
                .filter { it.key in ProductGridKeys.HEADER_KEYS }
                .sumOf { it.size.height }

            val remainingPx = (viewportPx - headersPx).coerceAtLeast(0)
            with(density) { remainingPx.toDp() }
        }
    }

    LazyVerticalGrid(
        state = lazyGridState,
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (banners.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }, key = ProductGridKeys.BANNERS) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    SmallBannerPager(
                        banners = banners,
                        onBannerClick = onBannerClick,
                        onAboutAdvertisingClick = onAboutAdvertisingClick
                    )
                }
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }, key = ProductGridKeys.TITLE) {
            ProductListTitle(
                modifier = Modifier.padding(bottom = 16.dp),
                productsQuantity = productsQuantity,
                title = title,
                showShare = showShare,
                onShareClick = onShareClick
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }, key = ProductGridKeys.CATEGORIES) {
            if (categories.isNotEmpty()) {
                ProductListCategoriesRow(
                    modifier = Modifier.padding(bottom = 16.dp),
                    categories = categories,
                    showEmptyCategory = showEmptyCategory,
                    currentCategory = currentCategory,
                    onCategoryClick = { category ->
                        onCategoryClick(category)
                    },
                    onCategoriesListClick = if (categoriesTree.allCategories().size > 1) {
                        onCategoriesListClick
                    } else null
                )
            }

        }

        stickyHeader(
            key = ProductGridKeys.OPTIONS
        ) {
            ProductListOptionsRow(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(top = 8.dp),
                sortName = currentSort.name,
                isGridView = isGridView,
                onFiltersClick = if (showFilters) onFiltersClick else null,
                onSortingClick = {
                    onSortingClick()
                },
                onSwitchClick = {
                    onSwitchLayoutClick()
                },
            )
        }

        val refreshLoadState = productsLoadStates.refresh
        if (refreshLoadState is LoadState.Error && refreshLoadState.error is EmptyResultException) {
            item(
                span = { GridItemSpan(maxLineSpan) },
                key = ProductGridKeys.EMPTY_PLACEHOLDER
            ) {
                val placeholder =
                    (refreshLoadState.error as? EmptyResultException)?.placeholder?.toUi()

                if (placeholder != null) {
                    VodovozPlaceholder(

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(
                                placeholderHeight.coerceAtLeast(1.dp)
                            ),
                        data = placeholder,
                        onProductClick = onProductClick,
                        onProductLike = onProductLike,
                        onAnalogsClick = onProductAnalogsClick,
                        onDecrementToCart = onDecrementProductToCart,
                        onIncrementToCart = onIncrementProductToCart,
                        onButtonClick = onPlacholderButtonClick
                    )
                }

            }
        } else {
            linearOrGridProducts(
                grid = isGridView,
                products = products,
                loadState = productsLoadStates,
                shimmerState = shimmer,
                onProductSee = onProductSee,
                onProductClick = onProductClick,
                onProductLike = onProductLike,
                onProductAnalogsClick = onProductAnalogsClick,
                onIncrementProductToCart = onIncrementProductToCart,
                onDecrementProductToCart = onDecrementProductToCart
            )
        }
    }
}

private object ProductGridKeys {
    const val BANNERS = "ProductBanners"
    const val TITLE = "ProductListTitle"
    const val CATEGORIES = "ProductListCategoriesRow"
    const val OPTIONS = "ProductListOptionsRow"
    const val EMPTY_PLACEHOLDER = "EmptyPlaceholder"

    val HEADER_KEYS = setOf(BANNERS, TITLE, CATEGORIES, OPTIONS)
}
