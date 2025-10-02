package com.m.vodovoz.domain.general.model.order

import com.m.vodovoz.domain.general.model.promotion.LabelModel
import com.m.vodovoz.domain.general.model.product.PriceModel

data class OrderProductModel(
    val id: Long,
    val name: String,
    val quantity: Int,
    val articleNumberText: String,
    val depositText: String?,
    val price: PriceModel?,
    val showcase: Boolean,
    val image: String,
    val labels: List<LabelModel>,
    val pricePerUnit: String?,
    val unitOfMeasurement: String?,
    val catalogQuantity: Int,
    val isFavorite: Boolean,
    val present: OrderProductPresentModel?,
    val restrictionCode: Int
)

data class OrderProductPresentModel(
    val title: String,
    val color: String,
)
