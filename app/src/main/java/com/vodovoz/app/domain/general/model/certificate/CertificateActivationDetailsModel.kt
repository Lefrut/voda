package com.vodovoz.app.domain.general.model.certificate

import com.vodovoz.app.domain.general.model.promotion.ColorfulButtonModel
import com.vodovoz.app.domain.general.model.FieldModel

data class CertificateActivationDetailsModel(
    val title: String,
    val field: FieldModel,
    val textHtml: String,
    val textUnderButtonHtml: String,
    val button: ColorfulButtonModel,
)
