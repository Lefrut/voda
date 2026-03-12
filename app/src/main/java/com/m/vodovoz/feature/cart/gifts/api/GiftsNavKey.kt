package com.m.vodovoz.feature.cart.gifts.api

import androidx.navigation3.runtime.NavKey
import com.m.vodovoz.feature.cart.model.CartPresentPopupWindowUi
import com.m.vodovoz.feature.cart.model.CartPresentUi

data class GiftsNavKey(
    val present: CartPresentUi? = null,
    val popupWindow: CartPresentPopupWindowUi,
) : NavKey {
    companion object {
        const val NAV_NAME: String = "feature/cart/gifts/Gifts"
    }
}
