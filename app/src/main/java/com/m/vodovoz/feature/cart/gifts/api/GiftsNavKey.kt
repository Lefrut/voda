package com.m.vodovoz.feature.cart.gifts.api

import androidx.navigation3.runtime.NavKey
import com.m.vodovoz.core.navigation.viewmodel.SharedViewModelStoreNavKey
import com.m.vodovoz.feature.cart.model.CartPresentPopupWindowUi
import com.m.vodovoz.feature.cart.model.CartPresentUi

data class GiftsNavKey(
    val present: CartPresentUi? = null,
    val popupWindow: CartPresentPopupWindowUi,
    override val parentContentKey: String? = null,
) : NavKey, SharedViewModelStoreNavKey {
    override fun toString(): String = parentContentKey
        ?: "GiftsNavKey(present=$present, popupWindow=$popupWindow)"

    companion object {
        const val NAV_NAME: String = "feature/cart/gifts/Gifts"
    }
}
