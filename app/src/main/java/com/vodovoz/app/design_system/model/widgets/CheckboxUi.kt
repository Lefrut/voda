package com.vodovoz.app.design_system.model.widgets

import androidx.compose.runtime.Immutable
import com.vodovoz.app.common.model.VodovozBoolean
import com.vodovoz.app.common.model.from
import com.vodovoz.app.domain.general.model.CheckBoxModel

@Immutable
data class CheckboxUi(
    override val id: String,
    val checked: Boolean,
    val isRequired: Boolean,
    val name: String,
) : WidgetUi(id) {
    override fun value(): String {
        return VodovozBoolean.from(checked).value
    }
}

fun List<CheckboxUi>.updateCheckbox(checkbox: CheckboxUi, newCheckbox: CheckboxUi): List<CheckboxUi> {
    if (checkbox.id != newCheckbox.id) return this

    val fieldIndex = indexOfFirst { checkbox.id == it.id }
    return toMutableList().apply {
        set(fieldIndex, newCheckbox)
    }
}


fun CheckBoxModel.toUi(): CheckboxUi {
    return CheckboxUi(
        id, checked, isRequired, name
    )
}

fun List<CheckBoxModel>.mapToUi(): List<CheckboxUi> {
    return map { it.toUi() }
}
