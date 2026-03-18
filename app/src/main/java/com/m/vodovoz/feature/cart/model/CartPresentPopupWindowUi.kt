package com.m.vodovoz.feature.cart.model

import android.os.Parcelable
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.domain.general.model.cart.CartPresentPopupWindowModel
import kotlinx.parcelize.Parcelize

@Parcelize
data class CartPresentPopupWindowUi(
    val title: String,
    val description: String,
    val items: List<CartPresentItemUi>,
    val button: ColorfulButtonUi,
    val present: CartPresentUi?
) : Parcelable

fun CartPresentPopupWindowModel.toUi(): CartPresentPopupWindowUi {
    return CartPresentPopupWindowUi(
        title = title,
        description = description,
        items = items.mapToUi(),
        button = button.toUi(),
        present = present?.toUi()
    )
}
