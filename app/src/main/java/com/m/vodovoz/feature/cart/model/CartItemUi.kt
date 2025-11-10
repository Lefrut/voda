package com.m.vodovoz.feature.cart.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.design_system.model.ForAdultsUi
import com.m.vodovoz.design_system.model.LabelUi
import com.m.vodovoz.design_system.model.VodovozItemUi
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.domain.general.model.cart.CartItemModel

@Immutable
data class CartItemUi(
    val itemId: Long,
    override val isFavorite: Boolean,
    override val cartQuantity: Int,
    override val cartLoading: Boolean,
    override val forAdults: ForAdultsUi?,
    override val id: Long,
    val productName: String,
    val canBuy: Boolean,
    val discountPercentsText: String,
    val discountPrice: Float,
    val priceText: String,
    val currentPrice: Float,
    val basePrice: Float,
    val depositText: String,
    val articleText: String,
    val image: String,
    val leftItems: Int,
    val label: LabelUi?,
    val hasDiscount: Boolean,
    val restriction: ProductRestrictionUi,
    val showcase: Boolean,
    val additionalProductsText: AdditionalProductsTextUi?,
) : VodovozItemUi<CartItemUi>() {
    override fun copyItem(
        forAdults: ForAdultsUi?,
        cartLoading: Boolean,
        isFavorite: Boolean,
        cartQuantity: Int,
        items: List<VodovozItemUi<*>>,
    ): CartItemUi = copy(
        cartQuantity = cartQuantity,
        cartLoading = cartLoading,
        isFavorite = isFavorite,
        forAdults = forAdults
    )
}


fun List<CartItemModel>.mapToUi(): List<CartItemUi> {
    return map { it.toUi() }
}

fun CartItemModel.toUi(): CartItemUi {
    return CartItemUi(
        itemId = id,
        id = productId,
        productName = productName,
        isFavorite = isFavorite,
        canBuy = canBuy,
        discountPercentsText = discountPercentsText,
        discountPrice = discountPrice,
        priceText = priceText,
        currentPrice = currentPrice,
        basePrice = basePrice,
        cartQuantity = quantity,
        depositText = depositText,
        articleText = articleText,
        image = image,
        leftItems = leftItems,
        label = label?.toUi(),
        hasDiscount = hasDiscount,
        restriction = ProductRestrictionUi.fromCode(restrictionsCode),
        showcase = showcase,
        cartLoading = false,
        forAdults = forAdults?.toUi(),
        additionalProductsText = additionalProductsText?.toUi()
    )
}
