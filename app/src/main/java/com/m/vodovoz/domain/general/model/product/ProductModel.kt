package com.m.vodovoz.domain.general.model.product

import com.m.vodovoz.domain.general.model.promotion.ColorfulButtonModel
import com.m.vodovoz.domain.general.model.user.ForAdultsModel
import com.m.vodovoz.domain.general.model.widgets.LabelModel

data class ProductModel(
    val id: Long,
    val name: String,
    val deposit: Int,
    val isFavorite: Boolean,
    val rating: Float,
    val picture: String,
    val coefficient: Float,
    val quantity: Int,
    val cartQuantity: Int,
    val pricePerUnit: String?,
    val unitOfMeasurement: String?,
    val firstPrice: PriceModel,
    val prices: List<PriceModel>,
    val labels: List<LabelModel>,
    val forAdults: ForAdultsModel?,
    val analogButton: ColorfulButtonModel?,
)

data class PriceModel(
    val price: Float,
    val oldPrice: Float,
    val quantityFrom: Int,
    val quantityTo: Int,
)