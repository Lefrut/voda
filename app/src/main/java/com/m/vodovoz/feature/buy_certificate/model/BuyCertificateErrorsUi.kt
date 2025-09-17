package com.m.vodovoz.feature.buy_certificate.model

import androidx.compose.runtime.Immutable

@Immutable
data class BuyCertificateErrorsUi(
    val certificate: Boolean = false,
    val payment: Boolean = false,
){
    companion object {
        val Empty = BuyCertificateErrorsUi()
    }
}
