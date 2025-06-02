package com.vodovoz.app.design_system.model.widgets

import androidx.compose.runtime.Immutable

@Immutable
data class SwitchUi(
    override val id: String,
    val name: String,
    val value: Boolean,
    val enabled: Boolean
): WidgetUi(id)
