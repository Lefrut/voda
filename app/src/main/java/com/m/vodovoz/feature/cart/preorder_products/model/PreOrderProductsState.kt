package com.m.vodovoz.feature.cart.preorder_products.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.model.ForAdultsUi
import com.m.vodovoz.design_system.model.ProductUi
import com.m.vodovoz.ui.paging.ItemsState

@Immutable
data class PreOrderProductsState(
    val title: String = "",
    val purchase: Boolean = false,
    val button: ColorfulButtonUi = ColorfulButtonUi.Empty,
    override val items: List<ProductUi> = emptyList(),
    val currentProductId: Long? = null,
    val previewImage: String? = null,
    val showForAdultsDialog: Boolean = false,
    val forAdultsDialog: ForAdultsUi = ForAdultsUi.Empty,
    val blockedAdultProductId: Long? = null,
) : ItemsState<ProductUi, PreOrderProductsState>() {

    override fun withItems(newItems: List<ProductUi>): PreOrderProductsState {
        return copy(items = newItems)
    }
}
