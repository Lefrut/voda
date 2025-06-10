package com.vodovoz.app.feature.product_analogs.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.domain.general.model.product.ProductsSectionUi
import com.vodovoz.app.feature.product_comments.model.SortUi

@Immutable
data class ProductAnalogsState(
    val productsSection: ProductsSectionUi = ProductsSectionUi.Empty,
    val currentSort: SortUi = SortUi.Empty,
    val showSortOptionsBottomSheet: Boolean = false,
    val isGridView: Boolean = true,
    val uiState: ProductAnalogsUiState = ProductAnalogsUiState.Loading
)

sealed interface ProductAnalogsUiState{

    data object Loading: ProductAnalogsUiState

    data object Success: ProductAnalogsUiState

    data object Error: ProductAnalogsUiState


}