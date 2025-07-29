package com.vodovoz.app.design_system.model.widgets

import androidx.compose.runtime.Immutable
import com.vodovoz.app.R
import com.vodovoz.app.common.model.VodovozAddressType

@Immutable
data class SingleCheckboxGroup<out T : Any>(
    override val id: String,
    val checkbox: SingleCheckbox<T>?,
    val checkboxes: List<SingleCheckbox<T>>
): WidgetUi(id) {

    override fun value(): String {
        return checkbox?.value?.toString() ?: ""
    }
}

@Immutable
data class SingleCheckbox<out T : Any>(
    val name: String,
    val value: T
)




