package com.m.vodovoz.feature.cart.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.domain.general.model.cart.AdditionalProductsTextModel

@Immutable
data class AdditionalProductsTextUi(
    val productsId: Long,
    val text: String,
    val articleNumber: String,
)

fun AdditionalProductsTextModel.toUi(): AdditionalProductsTextUi {
    return AdditionalProductsTextUi(
        productsId = productsId,
        text = text,
        articleNumber = articleNumber
    )
}