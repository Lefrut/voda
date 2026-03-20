package com.m.vodovoz.feature.cart.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.m.vodovoz.design_system.model.ForAdultsUi
import com.m.vodovoz.design_system.model.ProductUi
import com.m.vodovoz.design_system.model.VodovozItemUi
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.design_system.model.widgets.LabelUi
import com.m.vodovoz.design_system.model.widgets.toUi
import com.m.vodovoz.domain.general.model.cart.CartPresentItemModel
import com.m.vodovoz.util.formatRoundedPrice
import com.m.vodovoz.util.smartParseFloat
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
data class CartPresentItemUi(
    override val id: Long,
    val name: String,
    val image: String,
    val price: String?,
    val oldPrice: String?,
    val label: LabelUi?,
    val maxQuantity: Int,
    override val forAdults: ForAdultsUi?,
    override val cartLoading: Boolean = false,
    override val cartQuantity: Int = 0,
) : Parcelable, VodovozItemUi<CartPresentItemUi>() {
    companion object {
        val Empty = CartPresentItemUi(
            id = -1,
            name = "",
            image = "",
            price = "",
            oldPrice = "",
            label = null,
            maxQuantity = Int.MAX_VALUE,
            forAdults = null
        )
    }

    override fun copyItem(
        forAdults: ForAdultsUi?,
        cartLoading: Boolean,
        isFavorite: Boolean,
        cartQuantity: Int,
        items: List<VodovozItemUi<*>>,
    ): CartPresentItemUi {
        return copy(
            forAdults = forAdults,
            cartLoading = cartLoading,
            cartQuantity = cartQuantity
        )
    }
}

fun List<CartPresentItemModel>.mapToUi(): List<CartPresentItemUi> {
    return map { it.toUi() }
}

fun CartPresentItemModel.toUi(): CartPresentItemUi {
    return CartPresentItemUi(
        id = id,
        name = name,
        image = image,
        price = price,
        oldPrice = oldPrice,
        label = label?.toUi(),
        maxQuantity = maxQuantity,
        forAdults = forAdults?.toUi()
    )
}

fun List<CartPresentItemUi>.mapToProductUi(): List<ProductUi> = map { it.toProductUi() }

fun CartPresentItemUi.toProductUi(): ProductUi {
    return ProductUi(
        rating = 0f,
        price = price?.smartParseFloat() ?: 0f,
        oldPrice = oldPrice?.smartParseFloat() ?: 0f,
        name = name,
        image = image,
        labels = listOfNotNull(label),
        isAvailable = maxQuantity > 0,
        maxQuantity = maxQuantity,
        pricePerUnit = null,
        unitOfMeasurement = null,
        button = null,
        id = id,
        isFavorite = false,
        cartQuantity = cartQuantity,
        cartLoading = cartLoading,
        forAdults = forAdults
    )
}

fun ProductUi.toCartPresentItemUi(): CartPresentItemUi {
    return CartPresentItemUi(
        id = id,
        name = name,
        image = image,
        price = price.formatRoundedPrice(),
        oldPrice = oldPrice.takeIf { it > price }?.formatRoundedPrice(),
        label = labels.firstOrNull(),
        maxQuantity = maxQuantity,
        forAdults = forAdults,
        cartLoading = cartLoading,
        cartQuantity = cartQuantity
    )
}
