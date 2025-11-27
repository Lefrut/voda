package com.m.vodovoz.feature.product_details.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.m.vodovoz.design_system.composables.card.GridProductCard
import com.m.vodovoz.design_system.composables.layout.FixedGridFlowRow
import com.m.vodovoz.design_system.model.ProductUi
import com.m.vodovoz.design_system.model.SectionContentUi

@Composable
fun ProductDetailsAccessoryProducts(
    modifier: Modifier = Modifier,
    productSection: SectionContentUi<ProductUi>,
    onProductLike: (ProductUi) -> Unit,
    onProductClick: (ProductUi) -> Unit,
    onProductAnalogsClick: (ProductUi) -> Unit,
    onIncrementProductToCart: (ProductUi) -> Unit,
    onDecrementProductToCart: (ProductUi) -> Unit,
) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = productSection.title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineSmall
        )

        FixedGridFlowRow(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth(),
            horizontalSpacing = 10.dp,
            verticalSpacing = 8.dp,
            itemsInRow = 2
        ) { itemWidth ->
            productSection.items.forEach { product ->
                key(product.id) {
                    GridProductCard(
                        modifier = Modifier.weight(1f),
                        product = product,
                        onClick = onProductClick,
                        onLike = onProductLike,
                        onAnalogsClick = onProductAnalogsClick,
                        onIncrementToCart = onIncrementProductToCart,
                        onDecrementToCart = onDecrementProductToCart
                    )
                }
            }
            if (productSection.items.size % 2 == 1) {
                Spacer(modifier = Modifier.width(itemWidth))
            }
        }
    }

}