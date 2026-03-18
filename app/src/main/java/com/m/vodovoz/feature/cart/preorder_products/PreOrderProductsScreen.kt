package com.m.vodovoz.feature.cart.preorder_products

import androidx.compose.runtime.Composable
import com.m.vodovoz.feature.cart.composables.SelectableProductsScreen
import com.m.vodovoz.feature.cart.preorder_products.model.PreOrderProductsState

@Composable
fun PreOrderProductsScreen(
    viewModel: PreOrderProductsViewModel,
    viewState: PreOrderProductsState,
) {
    SelectableProductsScreen(
        title = viewState.title,
        items = viewState.items,
        selectedItems = listOfNotNull(viewState.currentProduct),
        purchase = viewState.purchase,
        showIndicator = false,
        button = viewState.button,
        present = viewState.present,
        onBackClick = viewModel::navigateBack,
        onItemClick = viewModel::selectProduct,
        onImageClick = { product -> viewModel.showPreviewImageDialog(product.image) },
        onIncrementClick = viewModel::incrementProduct,
        onDecrementClick = viewModel::decrementProduct,
        onButtonClick = viewModel::tryToAddProduct
    )
}
