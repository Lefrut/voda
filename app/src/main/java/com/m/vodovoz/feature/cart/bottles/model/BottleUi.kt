package com.m.vodovoz.feature.cart.bottles.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.m.vodovoz.domain.general.model.product.BottleModel
import com.m.vodovoz.feature.cart.model.CartItemUi
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
data class BottleUi(
    val name: String,
    val id: Long,
    val articleText: String,
    val description: String,
    val cartQuantity: Int,
) : Parcelable


fun List<BottleUi>.toMap(): Map<Long, Int> {
    return associate { it.id to it.cartQuantity }
}

fun CartItemUi.toBottle(): BottleUi {
    return BottleUi(
        name = productName,
        id = id,
        articleText = "",
        description = "",
        cartQuantity = cartQuantity
    )
}

fun BottleModel.toUi(): BottleUi {
    return BottleUi(name, id, articleText, description, cartQuantity)
}

fun List<BottleModel>.mapToUi(): List<BottleUi> {
    return map { it.toUi() }
}


fun List<BottleUi>.updateCartQuantity(
    bottle: BottleUi,
    block: (cartQuantity: Int) -> Int,
): List<BottleUi> {
    return map { b ->
        if (b.id == bottle.id) {
            b.copy(cartQuantity = block(b.cartQuantity).coerceAtLeast(0))
        } else {
            b
        }
    }
}
