package com.m.vodovoz.design_system.model.widgets

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.m.vodovoz.common.model.VodovozBoolean
import com.m.vodovoz.common.model.from
import com.m.vodovoz.domain.general.model.widgets.CheckboxModel
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
data class CheckboxUi(
    override val id: String,
    val checked: Boolean,
    val isRequired: Boolean,
    val name: String,
    val urlTitles: List<String>,
    val error: Boolean = false
) : WidgetUi(id), Parcelable {
    override fun value(): String {
        return VodovozBoolean.from(checked).value
    }
}

fun List<CheckboxUi>.updateCheckbox(
    checkbox: CheckboxUi,
    newCheckbox: CheckboxUi,
): List<CheckboxUi> {
    if (checkbox.id != newCheckbox.id) return this

    val fieldIndex = indexOfFirst { checkbox.id == it.id }
    return toMutableList().apply {
        set(fieldIndex, newCheckbox)
    }
}


fun CheckboxModel.toUi(): CheckboxUi {
    return CheckboxUi(
        id = id,
        checked = checked,
        isRequired = isRequired,
        name = name,
        urlTitles = urlTitles
    )
}

fun List<CheckboxModel>.mapToUi(): List<CheckboxUi> {
    return map { it.toUi() }
}
