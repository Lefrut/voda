package com.m.vodovoz.feature.preorder.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.m.vodovoz.design_system.composables.button.VodovozButtonsColumn
import com.m.vodovoz.design_system.composables.checkbox.VodovozCheckbox
import com.m.vodovoz.design_system.composables.snackbar.VodovozSnackbarHost
import com.m.vodovoz.design_system.composables.text_fields.VodovozTextFieldsColumn
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.model.widgets.CheckboxUi
import com.m.vodovoz.design_system.model.widgets.FieldUi

@Suppress("NonSkippableComposable")
@Composable
fun PreOrderBody(
    modifier: Modifier = Modifier,
    description: String,
    colorfulButton: ColorfulButtonUi,
    fields: List<FieldUi>,
    checkbox: CheckboxUi?,
    snackbarHostState: SnackbarHostState,
    onOrderSend: () -> Unit,
    onFieldValueChange: (FieldUi, String) -> Unit,
    onCheckboxClick: (Boolean) -> Unit,
    onUrlClick: (url: String, title: String) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 8.dp)
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
                modifier = Modifier.padding(horizontal = 16.dp),
                fields = fields,
                onFieldChange = { field, updatedField ->
                    onFieldValueChange(field, updatedField.value)
                },
            )

            checkbox?.let {
                VodovozCheckbox(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                    checkbox = checkbox,
                    onCheckboxClick = { _, updatedCheckbox ->
                        onCheckboxClick(updatedCheckbox.checked)
                    },
                    onUrlClick = onUrlClick
                )
            }


            VodovozButtonsColumn(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
                buttons = listOf(colorfulButton)
            ) {
                onOrderSend()
            }
        }

        VodovozSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

}
