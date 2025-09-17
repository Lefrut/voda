package com.m.vodovoz.feature.cart.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.domain.general.model.cart.CartButtonModel

@Immutable
data class CartButtonUi(
    val id: String,
    val image: String,
    val name: String,
    val enabled: Boolean = true
){
    companion object{
        val Empty = CartButtonUi("","","")
    }
}

fun CartButtonModel.toUi(): CartButtonUi{
    return CartButtonUi(
        id, image, name
    )
}
