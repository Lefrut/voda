package com.m.vodovoz.feature.cart.preorder_products.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.model.ForAdultsUi
import com.m.vodovoz.feature.cart.model.CartPresentItemUi
import com.m.vodovoz.feature.cart.model.CartPresentUi
import com.m.vodovoz.ui.paging.ItemsState

@Immutable
data class PreOrderProductsState(
    val title: String = "",
    val description: String = "",
    val button: ColorfulButtonUi = ColorfulButtonUi.Empty,
    override val items: List<CartPresentItemUi> = emptyList(),
    val currentProduct: CartPresentItemUi? = null,
    val present: CartPresentUi? = null,
    val showForAdultsDialog: Boolean = false,
    val forAdultsDialog: ForAdultsUi = ForAdultsUi.Empty,
    val previewImage: String? = null,
) : ItemsState<CartPresentItemUi, PreOrderProductsState>() {

    override fun withItems(newItems: List<CartPresentItemUi>): PreOrderProductsState {
        return copy(items = newItems)
    }
}
