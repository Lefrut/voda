package com.m.vodovoz.domain.general.model.cart

import com.m.vodovoz.domain.general.model.user.ForAdultsModel
import com.m.vodovoz.domain.general.model.widgets.LabelModel

data class CartItemModel(
    val id: Long,
    val productId: Long,
    val productName: String,
    val isFavorite: Boolean,
    val canBuy: Boolean,
    val discountPercentsText: String,
    val discountPrice: Float,
    val priceText: String,
    val currentPrice: Float,
    val basePrice: Float,
    val quantity: Int,
    val depositText: String,
    val articleText: String,
    val image: String,
    val leftItems: Int,
    val label: LabelModel?,
    val hasDiscount: Boolean,
    val restrictionsCode: Int,
    val showcase: Boolean,
    val forAdults: ForAdultsModel?,
    val additionalProductsText: AdditionalProductsTextModel?,
)
