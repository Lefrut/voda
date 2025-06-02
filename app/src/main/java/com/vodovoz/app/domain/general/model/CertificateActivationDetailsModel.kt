package com.vodovoz.app.domain.general.model

data class CertificateActivationDetailsModel(
    val title: String,
    val field: FieldModel,
    val textHtml: String,
    val textUnderButtonHtml: String,
    val button: ColorfulButtonModel,
)
