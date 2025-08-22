package com.vodovoz.app.feature.buy_certificate.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.domain.general.model.order.CertificatePaymentInfoModel

@Immutable
data class CertificatePaymentInfoUi(
    val id: Int,
    val name: String,
    val browser: Boolean,
    val url: String,
)


fun CertificatePaymentInfoModel.toUi(): CertificatePaymentInfoUi{
    return CertificatePaymentInfoUi(
        id, name, browser, url
    )
}