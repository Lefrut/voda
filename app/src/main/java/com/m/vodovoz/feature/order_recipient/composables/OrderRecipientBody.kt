package com.m.vodovoz.feature.order_recipient.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.m.vodovoz.design_system.composables.button.VodovozButtonsColumn
import com.m.vodovoz.design_system.composables.checkbox.VodovozCheckbox
import com.m.vodovoz.design_system.composables.text_fields.VodovozTextFieldsColumn
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.model.widgets.CheckboxUi
import com.m.vodovoz.design_system.model.widgets.FieldUi

@Suppress("NonSkippableComposable")
@Composable
fun OrderRecipientBody(
    modifier: Modifier = Modifier,
    fields: List<FieldUi>,
    checkboxes: List<CheckboxUi>,
    button: ColorfulButtonUi,
    onFieldChange: (FieldUi, FieldUi) -> Unit,
    onCheckboxChange: (CheckboxUi, CheckboxUi) -> Unit,
    onButtonClick: (ColorfulButtonUi) -> Unit,
    onUrlClick: (String, String) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        val columnModifier = Modifier.padding(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp
        )
        VodovozTextFieldsColumn(
            modifier = columnModifier,
            fields = fields,
            onFieldChange = onFieldChange,
        )

        Column(
            modifier = columnModifier,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            checkboxes.forEach { checkbox ->
                key(checkbox.id) {
                    VodovozCheckbox(
                        checkbox = checkbox,
                        onCheckboxClick = onCheckboxChange,
                        onUrlClick = onUrlClick
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        VodovozButtonsColumn(
            modifier = Modifier.padding(
                bottom = 32.dp,
                top = 24.dp,
                start = 16.dp,
                end = 16.dp
            ),
            buttons = listOf(button),
            onButtonClick = onButtonClick
        )
    }
}