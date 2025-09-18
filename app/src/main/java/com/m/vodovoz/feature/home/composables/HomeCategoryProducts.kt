package com.m.vodovoz.feature.home.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.m.vodovoz.common.model.ButtonAction
import com.m.vodovoz.design_system.composables.card.GridProductCard
import com.m.vodovoz.design_system.composables.chip.VodovozChip
import com.m.vodovoz.design_system.composables.decoration.SkeletonBox
import com.m.vodovoz.design_system.composables.tab_row.VodovozScrollableTabRow
import com.m.vodovoz.design_system.model.CategoryWithProductsUi
import com.m.vodovoz.design_system.model.ProductUi
import com.m.vodovoz.design_system.model.SectionContentUi
import com.m.vodovoz.util.extensions.indexOfOrNull
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer

@Composable
fun HomeCategoryProducts(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    categoryWithProductsId: Long,
    sectionCategoriesWithProducts: SectionContentUi<CategoryWithProductsUi>,
    onShowAllClick: (ButtonAction) -> Unit,
    onCategorySelect: (CategoryWithProductsUi) -> Unit,
    onProductClick: (ProductUi) -> Unit,
    onProductLike: (ProductUi) -> Unit,
    onIncrementToCart: (ProductUi) -> Unit,
    onDecrementToCart: (ProductUi) -> Unit,
    onProductAnalogsClick: (ProductUi) -> Unit,
) {

    val button = sectionCategoriesWithProducts.button
    val items = sectionCategoriesWithProducts.items

    Column(modifier = modifier) {
        if (sectionCategoriesWithProducts.title.isNotBlank()) {
            TitleAndButton(
                title = sectionCategoriesWithProducts.title,
                button = button,
                onShowAllClick = { onShowAllClick(it) }
            )
        }

        val currentCategoryWithProducts =
            items.find { it.id == categoryWithProductsId } ?: CategoryWithProductsUi.Empty

        if (items.size > 1) {
            VodovozScrollableTabRow(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                selectedTabIndex = items.indexOfOrNull(currentCategoryWithProducts) ?: 0,
                edgePadding = 16.dp,
                spacing = 8.dp
            ) {
                items.forEach { sectionWithProducts ->
                    key(sectionWithProducts.id) {
                        VodovozChip(
                            text = sectionWithProducts.name,
                            selected = categoryWithProductsId == sectionWithProducts.id,
                            onSelect = { onCategorySelect(sectionWithProducts) }
                        )
                    }
                }
            }
        }


        val shimmerState = rememberShimmer(ShimmerBounds.View)
        val itemWidth = 160.dp

        LazyRow(
            state = lazyListState,
            modifier = Modifier.padding(top = 16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentCategoryWithProducts.items.isEmpty()) {
                items(List(10) { it }) {
                    Box(
                        modifier = Modifier
                    ) {
                        GridProductCard(
                            modifier = Modifier.width(itemWidth),
                            product = ProductUi.Empty,
                            onClick = {},
                            onLike = {},
                            onIncrementToCart = {},
                            onDecrementToCart = { },
                            onAnalogsClick = {}
                        )
                        SkeletonBox(
                            shimmerState = shimmerState,
                            modifier = Modifier.matchParentSize()
                        )
                    }
                }
            } else {
                items(
                    items = currentCategoryWithProducts.items,
                    key = { it.id }
                ) { product ->
                    GridProductCard(
                        modifier = Modifier.width(itemWidth),
                        product = product,
                        onClick = onProductClick,
                        onLike = onProductLike,
                        onIncrementToCart = onIncrementToCart,
                        onDecrementToCart = onDecrementToCart,
                        onAnalogsClick = onProductAnalogsClick
                    )
                }
            }
        }

        LaunchedEffect(categoryWithProductsId) { lazyListState.animateScrollToItem(0) }

    }
}