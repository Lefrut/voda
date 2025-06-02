package com.vodovoz.app.domain.general.model

data class PreOrderSectionModel(
    val title: String,
    val fields: List<FieldModel>,
    val colorfulButton: ColorfulButtonModel,
)