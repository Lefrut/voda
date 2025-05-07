package com.vodovoz.app.domain.general.model.service

import com.vodovoz.app.domain.general.model.ColorfulButtonModel

data class ServiceDetailsModel(
    val id: Int,
    val name: String,
    val html: String,
    val image: String,
    val productsSection: ServiceProductsModel?,
    val button: ColorfulButtonModel?
)
