package com.vodovoz.app.feature.bottom.services.detail.model

import com.vodovoz.app.design_system.model.ProductUi
import com.vodovoz.app.design_system.model.mapToUi
import com.vodovoz.app.domain.general.model.service.ServiceProductsModel

data class ServiceProductsUi(
    val title: String,
    val coefficient: Int,
    val products: List<ProductUi>,
    val additionalProductId: String
)

fun ServiceProductsModel.toUi(): ServiceProductsUi{
    return ServiceProductsUi(
        title = title,
        coefficient = coefficient,
        products = products.mapToUi(),
        additionalProductId = additionalProductId
    )
}