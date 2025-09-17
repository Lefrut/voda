package com.m.vodovoz.feature.preorder.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.design_system.model.widgets.CheckboxUi
import com.m.vodovoz.design_system.model.widgets.FieldUi
import com.m.vodovoz.design_system.model.widgets.toUi
import com.m.vodovoz.domain.general.model.order.FormModel

@Immutable
data class FormUi(
    val title: String,
    val description: String,
    val fields: List<FieldUi>,
    val button: ColorfulButtonUi,
    val checkbox: CheckboxUi?
) {
    companion object {
        val Empty = FormUi("", "", emptyList(), ColorfulButtonUi.Empty, null)
    }
}

fun FormModel.toUi(): FormUi {
    return FormUi(
        title = title,
        description = description,
        fields = fields.map { field -> field.toUi() },
        button = button.toUi(),
        checkbox = checkbox?.toUi()
    )
}

