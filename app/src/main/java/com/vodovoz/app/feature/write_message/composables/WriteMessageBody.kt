package com.vodovoz.app.feature.write_message.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vodovoz.app.design_system.composables.text_fields.VodovozTextFieldsColumn
import com.vodovoz.app.design_system.model.widgets.FieldUi

@Suppress("NonSkippableComposable")
@Composable
fun WriteMessageBody(
    modifier: Modifier = Modifier,
    fields: List<FieldUi>,
    onFieldChange: (FieldUi, FieldUi) -> Unit,
) {
    Column(
        modifier = modifier
            .padding(top = 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
//        if (description.isNotBlank()) {
//            Text(
//                text = description,
//                modifier = Modifier.padding(
//                    bottom = 24.dp
//                ),
//                color = MaterialTheme.colorScheme.onBackground
//            )
//        }

        VodovozTextFieldsColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            fields = fields,
            onFieldChange = { field, updatedField ->
                onFieldChange(field, updatedField)
            }
        )
    }

}