package com.vodovoz.app.domain.general.model.product

import com.vodovoz.app.domain.general.model.order.CertificatePaymentInfoModel
import com.vodovoz.app.domain.general.model.exceptions.VodovozPlaceholderModel

data class BuyCertificateModel(
    val placeholder: VodovozPlaceholderModel,
    val payment: CertificatePaymentInfoModel
)
