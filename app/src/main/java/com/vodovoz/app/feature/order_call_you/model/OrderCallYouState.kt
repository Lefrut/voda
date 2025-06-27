package com.vodovoz.app.feature.order_call_you.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.design_system.model.ColorfulButtonUi

@Immutable
data class OrderCallYouState(
    val uiState: OrderCallYouUiState = OrderCallYouUiState.Loading,
    val title: String = "",
    val currentItem: CallYouItemUi = CallYouItemUi.Empty,
    val items: List<CallYouItemUi> = emptyList(),
    val button: ColorfulButtonUi = ColorfulButtonUi.Empty
)
