package com.m.vodovoz.domain.general.model.product

import com.m.vodovoz.domain.general.model.order.CertificatePaymentInfoModel
import com.m.vodovoz.domain.general.model.exceptions.VodovozPlaceholderModel

data class BuyCertificateModel(
    val placeholder: VodovozPlaceholderModel,
    val payment: CertificatePaymentInfoModel
)
