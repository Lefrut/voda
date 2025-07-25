package com.vodovoz.app.feature.product_details.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vodovoz.app.design_system.composables.card.GridProductCard
import com.vodovoz.app.design_system.model.ProductUi
import com.vodovoz.app.design_system.model.SectionUi

@Composable
fun ProductDetailsAccessoryProducts(
    modifier: Modifier = Modifier,
    productSection: SectionUi<ProductUi>,
    onProductLike: (ProductUi) -> Unit,
    onProductClick: (ProductUi) -> Unit,
    onProductAnalogsClick: (ProductUi) -> Unit,
    onIncrementProductToCart: (ProductUi) -> Unit,
    onDecrementProductToCart: (ProductUi) -> Unit,
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            text = productSection.title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineSmall
        )

        FlowRow(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 2,

            ) {
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
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }

}