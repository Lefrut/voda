package com.vodovoz.app.feature.addresses.add.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.vodovoz.app.design_system.composables.button.VodovozButtonsColumn
import com.vodovoz.app.design_system.composables.core.VodovozWidget
import com.vodovoz.app.design_system.composables.detectTap
import com.vodovoz.app.design_system.composables.text_fields.VodovozTextField
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.widgets.FieldUi
import com.vodovoz.app.design_system.model.widgets.SwitchUi
import com.vodovoz.app.design_system.model.widgets.WidgetUi

@Suppress("NonSkippableComposable")
@Composable
fun AddAddressBody(
    modifier: Modifier = Modifier,
    addressWidget: WidgetUi,
    gridWidgets: List<FieldUi>,
    linearWidgets: List<WidgetUi>,
    linearSwitches: List<SwitchUi>,
    button: ColorfulButtonUi,
    onWidgetChange: (WidgetUi, WidgetUi) -> Unit,
    onWidgetClick: (WidgetUi) -> Unit,
    onSaveClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 8.dp)
    ) {

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            key(addressWidget.id) {
                VodovozWidget(
                    modifier = Modifier.detectTap {
                        onWidgetClick(addressWidget)
                    },
                    widget = if (addressWidget is FieldUi) addressWidget.copy(readOnly = true) else addressWidget,
                    onWidgetChange = { _, _ -> }
                )
            }

            FlowRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                maxItemsInEachRow = 2,
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                gridWidgets.forEach { field ->
                    key(field.id) {
                        VodovozTextField(
                            modifier = Modifier
                                .weight(1f),
                            field = field,
                            onFieldChange = onWidgetChange,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        )

                    }
                }

                if (gridWidgets.size % 2 == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            if(linearSwitches.isNotEmpty()){
                Column {
                    linearSwitches.forEach { widget ->
                        key(widget.id) {
                            VodovozWidget(
                                widget = widget,
                                onWidgetChange = onWidgetChange
                            )
                        }
                    }
                }
            }

            linearWidgets.forEach { widget ->
                key(widget.id) {
                    VodovozWidget(
                        widget = widget,
                        onWidgetChange = onWidgetChange
                    )
                }
            }
        }


        Spacer(modifier = Modifier.weight(1f))

        key("Button") {
            VodovozButtonsColumn(
                buttons = listOf(button),
                onButtonClick = { onSaveClick() },
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 32.dp,
                    bottom = 24.dp
                )
            )
        }

    }

}