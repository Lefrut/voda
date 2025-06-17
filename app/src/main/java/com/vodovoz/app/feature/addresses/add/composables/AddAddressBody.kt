package com.vodovoz.app.feature.addresses.add.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.vodovoz.app.R
import com.vodovoz.app.design_system.composables.button.VodovozButton
import com.vodovoz.app.design_system.composables.text_fields.VodovozTextField
import com.vodovoz.app.design_system.model.widgets.FieldUi
import com.vodovoz.app.design_system.model.widgets.WidgetUi

@Suppress("NonSkippableComposable")
@Composable
fun AddAddressBody(
    modifier: Modifier = Modifier,
    fields: List<FieldUi>,
    widgets: List<WidgetUi>,
    onWidgetChange: (WidgetUi, WidgetUi) -> Unit,
    onSaveClick: () -> Unit,
) {
    val firstWidget = widgets.firstOrNull()
    val otherWidgets = (widgets - firstWidget).mapNotNull { widget -> widget }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 8.dp)
    ) {

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {



            FlowRow(
                maxItemsInEachRow = 2,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                fields.forEach { field ->
                    VodovozTextField(
                        modifier = Modifier.weight(1f),
                        field = field,
                        onFieldChange = onWidgetChange,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    if (fields.size % 2 == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        VodovozButton(
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                top = 32.dp,
                bottom = 24.dp
            ),
            text = stringResource(id = R.string.save),
            onClick = onSaveClick
        )


    }

}