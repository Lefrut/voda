package com.vodovoz.app.feature.past_purchases.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.vodovoz.app.design_system.composables.list.ProductListCategoriesRow
import com.vodovoz.app.design_system.composables.list.ProductListOptionsRow
import com.vodovoz.app.design_system.composables.list.linearOrGridProducts
import com.vodovoz.app.design_system.composables.placeholders.VodovozPlaceholder
import com.vodovoz.app.design_system.model.ProductUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.domain.general.model.exceptions.EmptyResultException
import com.vodovoz.app.feature.home.model.CategoryUi
import com.vodovoz.app.feature.product_comments.model.SortUi

@Suppress("NonSkippableComposable")
@Composable
fun PastPurchasesBody(
    modifier: Modifier = Modifier,
    lazyGridState: LazyGridState,
    categories: List<CategoryUi>,
    currentCategory: CategoryUi,
    currentSort: SortUi,
    isGridView: Boolean,
    products: List<ProductUi>,
    productsLoadStates: CombinedLoadStates,
    onProductSee: (Int) -> Unit,
    onSortingClick: () -> Unit,
    onSwitchLayoutClick: () -> Unit,
    onCategoryClick: (CategoryUi) -> Unit,
    onProductClick: (ProductUi) -> Unit,
    onProductLike: (ProductUi) -> Unit,
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
        item(span = { GridItemSpan(maxLineSpan) }, key = "ProductListCategoriesRow") {
            if (categories.isNotEmpty()) {
                ProductListCategoriesRow(
                    modifier = Modifier.padding(bottom = 16.dp),
                    categories = categories,
                    showEmptyCategory = categories.size > 1,
                    currentCategory = currentCategory,
                    onCategoryClick = { category ->
                        onCategoryClick(category)
                    },
                )
            }

        }

        stickyHeader(key = "ProductListOptionsRow") {
            ProductListOptionsRow(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(top = 8.dp),
                sortName = currentSort.name,
                isGridView = isGridView,
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
                val placeholder = remember {
                    (refreshLoadState.error as? EmptyResultException)?.placeholder?.toUi()
                } ?: return@item

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
