package com.m.vodovoz.feature.write_message.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.model.widgets.FieldUi
import com.m.vodovoz.feature.preorder.model.FormUi
import com.m.vodovoz.ui.mvi.FormState

@Immutable
data class WriteMessageState(
    val uiState: WriteMessageUiState = WriteMessageUiState.Loading,
    override val form: FormUi = FormUi.Empty
): FormState()
