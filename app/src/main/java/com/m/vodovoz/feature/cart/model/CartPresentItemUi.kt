package com.m.vodovoz.feature.cart.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.m.vodovoz.design_system.model.ForAdultsUi
import com.m.vodovoz.design_system.model.VodovozItemUi
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.domain.general.model.cart.CartPresentItemModel
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
data class CartPresentItemUi(
    override val id: Long,
    val name: String,
    val image: String,
    val price: String?,
    val oldPrice: String?,
    override val forAdults: ForAdultsUi?,
) : Parcelable, VodovozItemUi<CartPresentItemUi>() {
    companion object {
        val Empty = CartPresentItemUi(-1, "", "", "", "", null)
    }

    override fun copyItem(
        forAdults: ForAdultsUi?,
        cartLoading: Boolean,
        isFavorite: Boolean,
        cartQuantity: Int,
        items: List<VodovozItemUi<*>>,
    ): CartPresentItemUi {
        return copy(forAdults = forAdults)
    }
}

fun List<CartPresentItemModel>.mapToUi(): List<CartPresentItemUi> {
    return map { it.toUi() }
}

fun CartPresentItemModel.toUi(): CartPresentItemUi {
    return CartPresentItemUi(id, name, image, price, oldPrice, forAdults?.toUi())
}
