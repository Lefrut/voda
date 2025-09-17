package com.m.vodovoz.domain.general.model.product

import com.m.vodovoz.domain.general.model.widgets.FieldModel

data class BuyCertificateTabModel(
    val id: Int,
    val name: String,
    val fields: List<FieldModel>
)
