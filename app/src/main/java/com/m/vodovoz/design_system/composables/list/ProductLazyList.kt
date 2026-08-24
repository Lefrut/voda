package com.m.vodovoz.design_system.composables.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import com.valentinilk.shimmer.Shimmer
import com.m.vodovoz.design_system.composables.card.GridProductCard
import com.m.vodovoz.design_system.composables.card.LinearProductCard
import com.m.vodovoz.design_system.composables.decoration.SkeletonBox
import com.m.vodovoz.design_system.model.ProductUi


@Suppress("NonSkippableComposable")
@Composable
fun ProductLazyList(
    products: List<ProductUi>,
    isGridView: Boolean,
    modifier: Modifier = Modifier,
    showFavorite: Boolean = true,
    showRating: Boolean = true,
    onProductClick: (ProductUi) -> Unit,
    onProductLike: (ProductUi) -> Unit,
    onIncrementProductToCart: (ProductUi) -> Unit,
    onDecrementProductToCart: (ProductUi) -> Unit,
    onProductAnalogsClick: (ProductUi) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isGridView) {
            items(
                products.size,
                span = { GridItemSpan(1) },
            ) { i ->
                GridProductCard(
                    product = products[i],
                    onClick = onProductClick,
                    onLike = onProductLike,
                    showFavorite = showFavorite,
                    showRating = showRating,
                    modifier = Modifier.fillMaxWidth(),
                    onAnalogsClick = onProductAnalogsClick,
                    onDecrementToCart = onDecrementProductToCart,
                    onIncrementToCart = onIncrementProductToCart
                )
            }
        } else {
            items(
                products.size,
                span = { GridItemSpan(2) }
            ) { i ->
                LinearProductCard(
                    modifier = Modifier.fillMaxWidth(),
                    product = products[i],
                    onClick = onProductClick,
                    onLike = onProductLike,
                    showFavorite = showFavorite,
                    showRating = showRating,
                    onAnalogsClick = onProductAnalogsClick,
                    onDecrementToCart = onDecrementProductToCart,
                    onIncrementToCart = onIncrementProductToCart
                )
            }
        }
    }
}


fun LazyGridScope.linearOrGridProducts(
    grid: Boolean,
    products: List<ProductUi>,
    loadState: CombinedLoadStates,
    shimmerState: Shimmer,
    onProductSee: (Int) -> Unit,
    onProductClick: (ProductUi) -> Unit,
    onProductLike: (ProductUi) -> Unit,
    onProductAnalogsClick: (ProductUi) -> Unit,
    onIncrementProductToCart: (ProductUi) -> Unit,
    onDecrementProductToCart: (ProductUi) -> Unit,
) {
    if (grid) {
        gridProducts(
            products,
            loadState,
            shimmerState,
            onProductSee,
            onProductClick,
            onProductLike,
            onProductAnalogsClick,
            onIncrementProductToCart,
            onDecrementProductToCart
        )
    } else {
        linearProducts(
            products,
            loadState,
            shimmerState,
            onProductSee,
            onProductClick,
            onProductLike,
            onProductAnalogsClick,
            onIncrementProductToCart,
            onDecrementProductToCart
        )
    }

}

fun LazyGridScope.linearProducts(
    products: List<ProductUi>,
    loadState: CombinedLoadStates,
    shimmerState: Shimmer,
    onProductSee: (Int) -> Unit,
    onProductClick: (ProductUi) -> Unit,
    onProductLike: (ProductUi) -> Unit,
    onProductAnalogsClick: (ProductUi) -> Unit,
    onIncrementProductToCart: (ProductUi) -> Unit,
    onDecrementProductToCart: (ProductUi) -> Unit,
) {
    when (loadState.refresh) {
        is LoadState.NotLoading -> {
            items(
                count = products.size,
                span = { GridItemSpan(2) },
            ) { index ->
                val product = products[index]

                SideEffect { onProductSee(index) }

                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    LinearProductCard(
                        modifier = Modifier.fillMaxWidth(),
                        product = product,
                        onClick = onProductClick,
                        onLike = onProductLike,
                        onIncrementToCart = onIncrementProductToCart,
                        onDecrementToCart = onDecrementProductToCart,
                        onAnalogsClick = onProductAnalogsClick
                    )
                    if (index != products.lastIndex) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

            }

        }

        else -> {
            items(6, span = { GridItemSpan(2) }) { i ->
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    SkeletonBox(
                        shimmerState = shimmerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    )
                    if (i != products.size - 1) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

    }



    item(span = { GridItemSpan(2) }) {
        if (loadState.append is LoadState.Loading) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(modifier = Modifier.height(16.dp))
                SkeletonBox(
                    shimmerState = shimmerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

}


fun LazyGridScope.gridProducts(
    products: List<ProductUi>,
    loadState: CombinedLoadStates,
    shimmerState: Shimmer,
    onProductSee: (Int) -> Unit,
    onProductClick: (ProductUi) -> Unit,
    onProductLike: (ProductUi) -> Unit,
    onProductAnalogsClick: (ProductUi) -> Unit,
    onIncrementProductToCart: (ProductUi) -> Unit,
    onDecrementProductToCart: (ProductUi) -> Unit,
) {
    when (loadState.refresh) {
        is LoadState.NotLoading -> {
            items(
                count = products.size,
                span = { GridItemSpan(1) },
            ) { index ->
                SideEffect { onProductSee(index) }

                GridEdgePadding(isStartPadding = index % 2 == 0) {
                    GridProductCard(
                        product = products[index],
                        onClick = onProductClick,
                        onLike = onProductLike,
                        modifier = Modifier.fillMaxWidth(),
                        onDecrementToCart = onDecrementProductToCart,
                        onAnalogsClick = onProductAnalogsClick,
                        onIncrementToCart = onIncrementProductToCart
                    )
                    if (index != products.lastIndex) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

        }

        else -> {
            items(8) { index ->
                GridEdgePadding(isStartPadding = index % 2 == 0) {
                    SkeletonBox(
                        shimmerState = shimmerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(255.dp)
                    )
                    if (index != products.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }


    }

    val countAppend = (products.size % 2) + 2
    items(countAppend, span = { GridItemSpan(1) }) { index ->
        if (loadState.append is LoadState.Loading) {
            GridEdgePadding(isStartPadding = if (countAppend % 2 == 0) index % 2 == 0 else index % 2 == 1) {
                SkeletonBox(
                    shimmerState = shimmerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(255.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

}

@Composable
inline fun GridEdgePadding(
    isStartPadding: Boolean,
    modifier: Modifier = Modifier,
    isEndPadding: Boolean = !isStartPadding,
    padding: Dp = 16.dp,
    content: ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.padding(
            start = if (isStartPadding) padding else 0.dp,
            end = if (isEndPadding) padding else 0.dp
        )
    ) {
        content()
    }
}

@Composable
inline fun GridHorizontalPadding(
    isStartPadding: Boolean,
    modifier: Modifier = Modifier,
    isEndPadding: Boolean = !isStartPadding,
    outerPadding: Dp = 16.dp,
    betweenPadding: Dp = 8.dp,
    content: ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.padding(
            start = if (isStartPadding) {
                outerPadding
            } else {
                betweenPadding
            },
            end = if (isEndPadding) {
                outerPadding
            } else {
                0.dp
            }
        )
    ) {
        content()
    }
}
