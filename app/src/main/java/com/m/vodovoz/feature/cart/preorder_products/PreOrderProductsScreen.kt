package com.m.vodovoz.feature.cart.preorder_products

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.unit.dp
import com.m.vodovoz.design_system.composables.button.VodovozButton
import com.m.vodovoz.design_system.composables.list.ProductLazyList
import com.m.vodovoz.design_system.composables.top_bar.VodovozTopBar
import com.m.vodovoz.feature.cart.composables.SelectableProductsScreen
import com.m.vodovoz.feature.cart.model.toCartPresentItemUi
import com.m.vodovoz.feature.cart.preorder_products.model.PreOrderProductsState

@Composable
fun PreOrderProductsScreen(
    viewModel: PreOrderProductsViewModel,
    viewState: PreOrderProductsState,
) {
    if (!viewState.purchase) {
        val items = viewState.items.map { product -> product.toCartPresentItemUi() }
        SelectableProductsScreen(
            title = viewState.title,
            items = items,
            selectedItems = items.filter { it.id == viewState.currentProductId },
            button = viewState.button,
            present = null,
            showIndicator = false,
            onBackClick = viewModel::navigateBack,
            onItemClick = { item -> viewModel.selectProduct(item.id) },
            onImageClick = { item -> viewModel.showPreviewImageDialog(item.image) },
            onButtonClick = viewModel::tryToAddProduct
        )
    } else Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        VodovozTopBar(
            onBack = viewModel::navigateBack,
            title = viewState.title
        )

        ProductLazyList(
            modifier = Modifier.weight(1f),
            products = viewState.items,
            isGridView = true,
            showFavorite = false,
            onProductClick = {},
            onProductLike = {},
            onIncrementProductToCart = viewModel::incrementProduct,
            onDecrementProductToCart = viewModel::decrementProduct,
            onProductAnalogsClick = {}
        )

        VodovozButton(
            modifier = Modifier.padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
            text = viewState.button.name,
            isLoading = viewState.button.loading,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = viewState.button.backgroundColor.takeOrElse {
                    MaterialTheme.colorScheme.primary
                },
                contentColor = viewState.button.textColor.takeOrElse {
                    MaterialTheme.colorScheme.background
                }
            ),
            onClick = viewModel::tryToAddProduct
        )
    }
}
