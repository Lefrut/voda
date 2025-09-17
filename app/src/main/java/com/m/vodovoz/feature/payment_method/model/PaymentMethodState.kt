package com.m.vodovoz.feature.payment_method.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.model.SectionUi

@Immutable
data class PaymentMethodState(
    val title: String = "",
    val uiState: PaymentMethodUiState = PaymentMethodUiState.Loading,
    val button: ColorfulButtonUi = ColorfulButtonUi.Empty,
    val paymentSections: List<SectionUi<PaymentMethodItemUi>> = emptyList()
)
