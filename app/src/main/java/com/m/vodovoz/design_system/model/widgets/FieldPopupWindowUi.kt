package com.m.vodovoz.design_system.model.widgets

import androidx.compose.runtime.Immutable
import com.m.vodovoz.design_system.model.ColorfulButtonUi

@Immutable
data class FieldPopupWindowUi(
    val title: String,
    val description: String,
    val field: FieldUi,
    val button: ColorfulButtonUi,
)