package com.vodovoz.app.feature.order_recipient.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.design_system.model.ButtonUi
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.widgets.FieldUi

@Immutable
data class OrderRecipientState(
    val uiState: OrderRecipientUiState = OrderRecipientUiState.Loading,
    val title: String = "",
    val fields: List<FieldUi> = emptyList(),
    val button: ColorfulButtonUi = ColorfulButtonUi.Empty

)
