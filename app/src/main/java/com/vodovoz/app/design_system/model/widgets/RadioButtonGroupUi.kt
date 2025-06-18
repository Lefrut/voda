package com.vodovoz.app.design_system.model.widgets

import androidx.compose.runtime.Immutable

@Immutable
data class RadioButtonGroupUi<out T : Any>(
    override val id: String,
    val option: RadioOptionUi<T>,
    val options: List<RadioOptionUi<T>>,
) : WidgetUi(id)


@Immutable
data class RadioOptionUi<out T>(
    val name: String,
    val value: T,
)