package com.m.vodovoz.domain.general.model.service

import com.m.vodovoz.domain.general.model.promotion.ColorfulButtonModel

data class ServiceDetailsModel(
    val id: Int,
    val name: String,
    val html: String,
    val image: String,
    val productsSection: ServiceProductsModel?,
    val button: ColorfulButtonModel?
)
