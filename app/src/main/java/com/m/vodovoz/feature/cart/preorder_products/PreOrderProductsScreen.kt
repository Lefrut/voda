package com.m.vodovoz.feature.cart.preorder_products

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.m.vodovoz.R
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
        selectedItem = viewState.currentProduct,
        showIndicator = false,
        button = viewState.button,
        present = viewState.present,
        onBackClick = viewModel::navigateBack,
        onItemClick = viewModel::selectProduct,
        onImageClick = { product -> viewModel.showPreviewImageDialog(product.image) },
        onButtonClick = viewModel::tryToAddProduct
    )
}
