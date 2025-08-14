package com.vodovoz.app.feature.product_catalog.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.vodovoz.app.design_system.composables.list.ProductListCategoriesRow
import com.vodovoz.app.design_system.composables.list.ProductListOptionsRow
import com.vodovoz.app.design_system.composables.list.ProductListTitle
import com.vodovoz.app.design_system.composables.list.linearOrGridProducts
import com.vodovoz.app.design_system.composables.placeholders.VodovozPlaceholder
import com.vodovoz.app.design_system.model.ParentCategoryUi
import com.vodovoz.app.design_system.model.ProductUi
import com.vodovoz.app.design_system.model.allCategories
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.domain.general.model.EmptyResultException
import com.vodovoz.app.feature.home.model.CategoryUi
import com.vodovoz.app.feature.product_comments.model.SortUi

@Suppress("NonSkippableComposable")
@Composable
fun ProductCatalogBody(
    modifier: Modifier = Modifier,
    lazyGridState: LazyGridState,
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
) {
    val shimmer = rememberShimmer(shimmerBounds = ShimmerBounds.View)

    LazyVerticalGrid(
        state = lazyGridState,
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }, key = "ProductListTitle") {
            ProductListTitle(
                modifier = Modifier.padding(bottom = 16.dp),
                productsQuantity = productsQuantity,
                title = title,
                showShare = showShare,
                onShareClick = onShareClick
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }, key = "ProductListCategoriesRow") {
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
            key = "ProductListOptionsRow"
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
            item(span = { GridItemSpan(2) }) {
                val placeholder =
                    (refreshLoadState.error as? EmptyResultException)?.placeholder?.toUi()
                        ?: return@item

                VodovozPlaceholder(data = placeholder)
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
