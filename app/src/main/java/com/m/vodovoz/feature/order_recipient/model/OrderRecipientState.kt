package com.m.vodovoz.feature.order_recipient.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.model.widgets.CheckboxUi
import com.m.vodovoz.design_system.model.widgets.FieldUi

@Immutable
data class OrderRecipientState(
    val uiState: OrderRecipientUiState = OrderRecipientUiState.Loading,
    val title: String = "",
    val fields: List<FieldUi> = emptyList(),
    val button: ColorfulButtonUi = ColorfulButtonUi.Empty,
    val checkboxes: List<CheckboxUi> = emptyList(),

    )
