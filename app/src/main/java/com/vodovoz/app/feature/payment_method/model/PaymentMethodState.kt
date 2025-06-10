package com.vodovoz.app.feature.payment_method.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.SectionUi

@Immutable
data class PaymentMethodState(
    val title: String = "",
    val uiState: PaymentMethodUiState = PaymentMethodUiState.Loading,
    val button: ColorfulButtonUi = ColorfulButtonUi.Empty,
    val paymentSections: List<SectionUi<PaymentMethodItemUi>> = emptyList()
)
