package com.m.vodovoz.design_system.model.widgets

import androidx.compose.runtime.Immutable

@Immutable
data class RadioButtonGroupUi<out T : Any>(
    override val id: String,
    val option: RadioOptionUi<T>,
    val options: List<RadioOptionUi<T>>,
) : WidgetUi(id) {
    override fun value(): String {
        return option.value.toString() ?: ""
    }
}


@Immutable
data class RadioOptionUi<out T>(
    val name: String,
    val value: T,
)