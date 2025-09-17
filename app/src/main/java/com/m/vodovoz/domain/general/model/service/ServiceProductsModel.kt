package com.m.vodovoz.domain.general.model.service

import com.m.vodovoz.domain.general.model.product.ProductModel

data class ServiceProductsModel(
    val title: String,
    val coefficient: Int,
    val products: List<ProductModel>,
    val additionalProductId: String
)
