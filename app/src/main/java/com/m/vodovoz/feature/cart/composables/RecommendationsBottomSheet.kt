package com.m.vodovoz.feature.cart.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.dp
import com.m.vodovoz.design_system.composables.bottom_sheet.VodovozDragHandle
import com.m.vodovoz.design_system.composables.button.VodovozButtonsColumn
import com.m.vodovoz.design_system.composables.card.GridProductCard
import com.m.vodovoz.design_system.model.ProductUi
import com.m.vodovoz.design_system.vodovozTextLinkStyle
import com.m.vodovoz.feature.cart.model.AdditionalProductsBSUi
import com.m.vodovoz.util.extensions.indexOfOrZero

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationsBottomSheet(
    modifier: Modifier = Modifier,
    state: SheetState = rememberModalBottomSheetState(true),
    additionalProductsBS: AdditionalProductsBSUi,
    onDismissRequest: () -> Unit,
    onProductSee: (Int) -> Unit,
    onProductClick: (ProductUi) -> Unit,
    onProductLike: (ProductUi) -> Unit,
    onProductAnalogsClick: (ProductUi) -> Unit,
    onProductIncrementToCart: (ProductUi) -> Unit,
    onProductDecrementToCart: (ProductUi) -> Unit,
) {
    ModalBottomSheet(
        modifier = modifier,
        sheetState = state,
        onDismissRequest = onDismissRequest,
        dragHandle = { VodovozDragHandle() },
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                modifier = Modifier.padding(
                    top = 8.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                text = additionalProductsBS.title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium
            )
            val products = additionalProductsBS.products
            val contentModifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
            LazyRow(
                modifier = contentModifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp)
            ) {
                items(
                    items = products,
                    key = { productUi ->
                        productUi.id
                    }
                ) { product ->
                    GridProductCard(
                        modifier = Modifier
                            .width(160.dp)
                            .wrapContentHeight(),
                        product = product,
                        onClick = onProductClick,
                        onAnalogsClick = onProductAnalogsClick,
                        onDecrementToCart = onProductDecrementToCart,
                        onIncrementToCart = onProductIncrementToCart,
                        onLike = onProductLike
                    )

                    SideEffect {
                        onProductSee(products.indexOfOrZero(product))
                    }
                }
            }

            if (additionalProductsBS.description.isNotEmpty()) {
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = AnnotatedString.fromHtml(
                        htmlString = additionalProductsBS.description,
                        linkStyles = vodovozTextLinkStyle
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            additionalProductsBS.button?.let { btn ->
                VodovozButtonsColumn(
                    modifier = Modifier.padding(
                        top = 20.dp, start = 16.dp, end = 16.dp
                    ),
                    buttons = listOf(btn),
                    onButtonClick = { onDismissRequest() }
                )
            }

            Spacer(Modifier.height(18.dp))
        }
    }
}