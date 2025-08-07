package com.vodovoz.app.feature.service_order.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vodovoz.app.design_system.composables.button.VodovozButtonsColumn
import com.vodovoz.app.design_system.composables.text_fields.VodovozTextFieldsColumn
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.widgets.FieldUi

@Suppress("NonSkippableComposable")
@Composable
fun ServiceOrderBody(
    modifier: Modifier = Modifier,
    subtitle: String,
    fields: List<FieldUi>,
    button: ColorfulButtonUi,
    onFieldChange: (FieldUi, FieldUi) -> Unit,
    onButtonClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 8.dp)
    ) {
        if (subtitle.isNotBlank()) {
            Text(
                modifier = Modifier.padding(bottom = 24.dp),
                text = subtitle,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodySmall
            )
        }

        VodovozTextFieldsColumn(
            fields = fields,
            onFieldChange = onFieldChange,
            onDone = {},
        )

        Spacer(modifier = Modifier.weight(1f))

        VodovozButtonsColumn(
            modifier = Modifier.padding(vertical = 24.dp),
            buttons = listOf(button),
            onButtonClick = {
                onButtonClick()
            }
        )
    }
}