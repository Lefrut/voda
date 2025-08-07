package com.vodovoz.app.feature.order_recipient.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vodovoz.app.design_system.composables.button.VodovozButtonsColumn
import com.vodovoz.app.design_system.composables.text_fields.VodovozTextFieldsColumn
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.widgets.FieldUi

@Suppress("NonSkippableComposable")
@Composable
fun OrderRecipientBody(
    modifier: Modifier = Modifier,
    fields: List<FieldUi>,
    button: ColorfulButtonUi,
    onFieldChange: (FieldUi, FieldUi) -> Unit,
    onButtonClick: (ColorfulButtonUi) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        VodovozTextFieldsColumn(
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp
            ),
            fields = fields,
            onFieldChange = onFieldChange,
        )

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