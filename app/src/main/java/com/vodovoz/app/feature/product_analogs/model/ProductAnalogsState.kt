package com.vodovoz.app.feature.product_analogs.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.vodovoz.app.design_system.model.ProductUi
import com.vodovoz.app.domain.general.model.product.ProductsSectionUi
import com.vodovoz.app.feature.product_comments.model.SortUi
import com.vodovoz.app.ui.paging.ItemsState

@Immutable
data class ProductAnalogsState(
    val productsSection: ProductsSectionUi = ProductsSectionUi.Empty,
    val currentSort: SortUi = SortUi.Empty,
    val showSortOptionsBottomSheet: Boolean = false,
    val isGridView: Boolean = true,
    val uiState: ProductAnalogsUiState = ProductAnalogsUiState.Loading,
    override val items: List<ProductUi> = emptyList(),
) : ItemsState<ProductUi, ProductAnalogsState>() {
    override fun withItems(newItems: List<ProductUi>): ProductAnalogsState =
        copy(
            items = newItems,
            productsSection = productsSection.copy(products = newItems)
        )

}

@Stable
sealed interface ProductAnalogsUiState {

    data object Loading : ProductAnalogsUiState

    data object Success : ProductAnalogsUiState

    data object Error : ProductAnalogsUiState


}