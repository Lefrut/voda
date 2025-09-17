package com.m.vodovoz.feature.order_question.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.model.widgets.FieldUi

@Immutable
data class OrderQuestionState(
    val title: String = "",
    val description: String = "",
    val fields: List<FieldUi> = emptyList(),
    val uiState: OrderQuestionUiState = OrderQuestionUiState.Loading,
    val button: ColorfulButtonUi = ColorfulButtonUi.Empty,
)
