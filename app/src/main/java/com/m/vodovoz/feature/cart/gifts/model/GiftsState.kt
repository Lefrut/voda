package com.m.vodovoz.feature.cart.gifts.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.model.ForAdultsUi
import com.m.vodovoz.feature.cart.model.CartPresentItemUi
import com.m.vodovoz.feature.cart.model.CartPresentUi
import com.m.vodovoz.ui.paging.ItemsState

@Immutable
data class GiftsState(
    val value: String = "",
    val button: ColorfulButtonUi = ColorfulButtonUi.Empty,
    override val items: List<CartPresentItemUi> = emptyList(),
    val currentGift: CartPresentItemUi = CartPresentItemUi.Empty,
    val present: CartPresentUi? = null,
    val showForAdultsDialog: Boolean = false,
    val forAdultsDialog: ForAdultsUi = ForAdultsUi.Empty,
) : ItemsState<CartPresentItemUi, GiftsState>() {

    override fun withItems(newItems: List<CartPresentItemUi>): GiftsState {
        return copy(items = newItems)
    }
}
