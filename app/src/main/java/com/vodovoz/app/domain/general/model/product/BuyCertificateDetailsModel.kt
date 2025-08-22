package com.vodovoz.app.domain.general.model.product

import com.vodovoz.app.domain.general.model.promotion.ColorfulButtonModel

data class BuyCertificateDetailsModel(
    val codes: BuyCertificateCodesModel,
    val title: String,
    val certificatesTitle: String,
    val certificates: List<CertificateModel>,
    val tabs: List<BuyCertificateTabModel>,
    val button: ColorfulButtonModel,
    val paymentTitle: String,
    val paymentTypes: List<PaymentTypeModel>,
    val faq: FAQModel?,
)
