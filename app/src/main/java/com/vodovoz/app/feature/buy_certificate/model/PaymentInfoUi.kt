package com.vodovoz.app.feature.buy_certificate.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.domain.general.model.PaymentInfoModel

@Immutable
data class PaymentInfoUi(
    val id: Int,
    val name: String,
    val browser: Boolean,
    val url: String,
)


fun PaymentInfoModel.toUi(): PaymentInfoUi{
    return PaymentInfoUi(
        id, name, browser, url
    )
}