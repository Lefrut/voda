package com.vodovoz.app.feature.write_message.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.widgets.FieldUi

@Immutable
data class WriteMessageState(
    val fields: List<FieldUi> = emptyList(),
    val title: String = "",
    val button: ColorfulButtonUi = ColorfulButtonUi.Empty,
    val uiState: WriteMessageUiState = WriteMessageUiState.Loading,
    val description: String = ""
)
