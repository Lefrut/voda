package com.vodovoz.app.feature.write_message.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.widgets.FieldUi
import com.vodovoz.app.feature.preorder.model.FormUi
import com.vodovoz.app.ui.mvi.FormState

@Immutable
data class WriteMessageState(
    val uiState: WriteMessageUiState = WriteMessageUiState.Loading,
    override val form: FormUi = FormUi.Empty
): FormState()
