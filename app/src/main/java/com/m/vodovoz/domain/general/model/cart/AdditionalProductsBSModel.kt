package com.m.vodovoz.domain.general.model.cart

import com.m.vodovoz.domain.general.model.product.ProductModel
import com.m.vodovoz.domain.general.model.promotion.ColorfulButtonModel

data class AdditionalProductsBSModel(
    val title: String,
    val description: String,
    val products: List<ProductModel>,
    val button: ColorfulButtonModel?,
)
