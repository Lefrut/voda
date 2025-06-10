package com.vodovoz.app.domain.general.model.service

import com.vodovoz.app.domain.general.model.product.ProductModel

data class ServiceProductsModel(
    val title: String,
    val coefficient: Int,
    val products: List<ProductModel>,
    val additionalProductId: String
)
