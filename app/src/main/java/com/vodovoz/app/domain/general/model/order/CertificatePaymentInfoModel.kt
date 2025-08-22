package com.vodovoz.app.domain.general.model.order

data class CertificatePaymentInfoModel(
    val id: Int,
    val name: String,
    val browser: Boolean,
    val url: String,
)
