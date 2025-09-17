package com.m.vodovoz.feature.cart.gifts.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.feature.cart.model.CartPresentItemUi
import com.m.vodovoz.feature.cart.model.CartPresentUi

@Immutable
data class GiftsState(
    val value: String = "",
    val button: ColorfulButtonUi = ColorfulButtonUi.Empty,
    val gifts: List<CartPresentItemUi> = emptyList(),
    val currentGift: CartPresentItemUi = CartPresentItemUi.Empty,
    val present: CartPresentUi? = null
)
