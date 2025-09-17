package com.m.vodovoz.feature.cart.gifts.model

import com.m.vodovoz.feature.cart.model.CartPresentItemUi

sealed interface GiftsEvent {
    data class GoToCart(val currentGift: CartPresentItemUi) : GiftsEvent

    data object GoBack: GiftsEvent

}