package com.vodovoz.app.feature.preorder.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.design_system.model.widgets.FieldUi
import com.vodovoz.app.design_system.model.widgets.toUi
import com.vodovoz.app.domain.general.model.order.FormModel

@Immutable
data class FormUi(
    val title: String,
    val fields: List<FieldUi>,
    val colorfulButton: ColorfulButtonUi,
) {
    companion object {
        val Empty = FormUi("", emptyList(), ColorfulButtonUi.Empty)
    }
}

fun FormModel.toUi(): FormUi {
    return FormUi(
        title = title,
        fields = fields.map { field -> field.toUi() },
        colorfulButton = button.toUi()
    )
}

