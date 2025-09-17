package com.m.vodovoz.feature.buy_certificate.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.domain.general.model.product.BuyCertificateCodesModel

@Immutable
data class BuyCertificateCodesUi(
    val certificates: String,
    val tabs: String,
    val payment: String,
){
    companion object{
        val Empty = BuyCertificateCodesUi("","", "")
    }
}

fun BuyCertificateCodesModel.toUi(): BuyCertificateCodesUi{
    return BuyCertificateCodesUi(
        certificates, tabs, payment
    )
}


