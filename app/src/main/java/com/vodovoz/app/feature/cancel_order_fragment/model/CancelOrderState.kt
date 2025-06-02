package com.vodovoz.app.feature.cancel_order_fragment.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.widgets.FieldUi

@Immutable
data class CancelOrderState(
    val uiState: CancelOrderUiState = CancelOrderUiState.Loading,
    val title: String = "",
    val warningText: String = "",
    val description: String = "",
    val checkboxesGroupId: String = "",
    val checkboxesNames: List<String> = emptyList(),
    val currentCheckboxName: String = "",
    val button: ColorfulButtonUi = ColorfulButtonUi.Empty,
    val commentField: FieldUi? = null
)
