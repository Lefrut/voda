package com.vodovoz.app.feature.write_message.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vodovoz.app.design_system.composables.text_fields.VodovozTextFieldsColumn
import com.vodovoz.app.design_system.model.widgets.FieldUi

@Suppress("NonSkippableComposable")
@Composable
fun WriteMessageBody(
    modifier: Modifier = Modifier,
    description: String,
    fields: List<FieldUi>,
    onFieldChange: (FieldUi, FieldUi) -> Unit,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(top = 8.dp, start = 16.dp, end = 16.dp)

    ) {
        if (description.isNotBlank()) {
            Text(
                modifier = Modifier.padding(bottom = 24.dp),
                text = description,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodySmall
            )
        }

        VodovozTextFieldsColumn(
            fields = fields,
            onFieldChange = { field, updatedField ->
                onFieldChange(field, updatedField)
            },
        )
    }

}