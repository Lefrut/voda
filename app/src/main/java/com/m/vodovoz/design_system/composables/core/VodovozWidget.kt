package com.m.vodovoz.design_system.composables.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.m.vodovoz.design_system.composables.checkbox.VodovozCheckbox
import com.m.vodovoz.design_system.composables.checkbox.VodovozSingleCheckboxColumn
import com.m.vodovoz.design_system.composables.radio_button.VodovozRadioButtonColumn
import com.m.vodovoz.design_system.composables.swich.VodovozSwitch
import com.m.vodovoz.design_system.composables.text_fields.VodovozTextField
import com.m.vodovoz.design_system.model.widgets.CheckboxUi
import com.m.vodovoz.design_system.model.widgets.FieldUi
import com.m.vodovoz.design_system.model.widgets.RadioButtonGroupUi
import com.m.vodovoz.design_system.model.widgets.SingleCheckboxGroup
import com.m.vodovoz.design_system.model.widgets.SwitchUi
import com.m.vodovoz.design_system.model.widgets.WidgetUi

@Composable
fun VodovozWidget(
    modifier: Modifier = Modifier,
    widget: WidgetUi,
    onWidgetChange: (widget: WidgetUi, updatedWidget: WidgetUi) -> Unit,
    onUrlClick: (url: String, title: String) -> Unit = { _, _ -> },
) {
    Box(modifier = modifier) {
        when (widget) {
            is FieldUi -> {
                VodovozTextField(
                    field = widget,
                    onFieldChange = onWidgetChange,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            is RadioButtonGroupUi<*> -> {
                VodovozRadioButtonColumn(
                    radioButtonGroup = widget,
                    onOptionClick = onWidgetChange
                )
            }

            is SwitchUi -> {
                VodovozSwitch(
                    switch = widget,
                    onSwitchChange = onWidgetChange
                )
            }

            is SingleCheckboxGroup<*> -> {
                VodovozSingleCheckboxColumn(
                    singleCheckboxGroup = widget,
                    onCheckboxClick = onWidgetChange
                )
            }

            is CheckboxUi -> {
                VodovozCheckbox(
                    checkbox = widget,
                    onCheckboxClick = onWidgetChange,
                    onUrlClick = onUrlClick
                )
            }
        }
    }
}