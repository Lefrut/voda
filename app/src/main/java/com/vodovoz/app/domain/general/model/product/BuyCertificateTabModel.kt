package com.vodovoz.app.domain.general.model.product

import com.vodovoz.app.domain.general.model.widgets.FieldModel

data class BuyCertificateTabModel(
    val id: Int,
    val name: String,
    val fields: List<FieldModel>
)
