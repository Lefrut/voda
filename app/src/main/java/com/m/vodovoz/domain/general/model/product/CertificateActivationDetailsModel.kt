package com.m.vodovoz.domain.general.model.product

import com.m.vodovoz.domain.general.model.promotion.ColorfulButtonModel
import com.m.vodovoz.domain.general.model.widgets.FieldModel

data class CertificateActivationDetailsModel(
    val title: String,
    val field: FieldModel,
    val textHtml: String,
    val textUnderButtonHtml: String,
    val button: ColorfulButtonModel,
)
