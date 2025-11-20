package com.m.vodovoz.feature.questionnaires.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m.vodovoz.design_system.composables.button.VodovozButtonsColumn
import com.m.vodovoz.design_system.composables.button.VodovozRadioButton
import com.m.vodovoz.design_system.composables.decoration.VodovozHorizontalDivider
import com.m.vodovoz.design_system.composables.text_fields.VodovozTextField
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.model.widgets.FieldUi
import com.m.vodovoz.feature.questionnaires.model.CheckboxListUi
import com.m.vodovoz.feature.questionnaires.model.ComponentOptionUi
import com.m.vodovoz.feature.questionnaires.model.ConditionUi
import com.m.vodovoz.feature.questionnaires.model.ConditionsCheckboxListUi
import com.m.vodovoz.feature.questionnaires.model.FieldComponentUi
import com.m.vodovoz.feature.questionnaires.model.OptionComponentUi
import com.m.vodovoz.feature.questionnaires.model.QuestionnaireComponentUi
import com.m.vodovoz.feature.questionnaires.model.SwitchUi
import com.m.vodovoz.feature.questionnaires.model.ToggleListUi

@Suppress("NonSkippableComposable")
@Composable
fun QuestionnairesBody(
    modifier: Modifier = Modifier,
    components: List<QuestionnaireComponentUi>,
    button: ColorfulButtonUi,
    onButtonClick: (ColorfulButtonUi) -> Unit,
    onConditionClick: (ConditionUi) -> Unit,
    onFieldChange: (FieldComponentUi, FieldUi) -> Unit,
    onOptionChange: (QuestionnaireComponentUi, ComponentOptionUi) -> Unit,
    onSwitchChange: (SwitchUi, String) -> Unit,
    onFieldClick: (FieldComponentUi) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            components.forEachIndexed { index, component ->
                key(component.id) {
                    if (component !is FieldComponentUi && index != components.lastIndex) {
                        VodovozHorizontalDivider()
                    }

                    when (component) {
                        is CheckboxListUi -> {
                            CheckboxListComponent(
                                ui = component,
                                onClick = onOptionChange
                            )
                        }

                        is FieldComponentUi -> {
                            VodovozTextField(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .pointerInput(Unit) {
                                        awaitEachGesture {
                                            awaitPointerEvent(PointerEventPass.Initial)
                                            onFieldClick(component)
                                        }
                                    },
                                field = component.ui,
                                onFieldChange = { _, updatedField ->
                                    onFieldChange(component, updatedField)
                                },
                            )
                        }

                        is SwitchUi -> {
                            SwitchComponent(
                                ui = component,
                                onClick = onSwitchChange
                            )
                        }

                        is ToggleListUi -> {
                            ToggleListComponent(
                                ui = component,
                                onClick = onOptionChange
                            )
                        }

                        is ConditionsCheckboxListUi -> {
                            ConditionCheckboxListComponent(
                                ui = component,
                                onClick = onOptionChange,
                                onConditionClick = onConditionClick
                            )
                        }
                    }
                }
            }
        }

        VodovozButtonsColumn(
            buttons = listOf(button),
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 24.dp),
            onButtonClick = onButtonClick
        )
    }
}

@Composable
private fun ConditionCheckboxListComponent(
    modifier: Modifier = Modifier,
    ui: ConditionsCheckboxListUi,
    onClick: (ConditionsCheckboxListUi, ComponentOptionUi) -> Unit,
    onConditionClick: (ConditionUi) -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = ui.label,
            color = if (ui.error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceTint,
            style = MaterialTheme.typography.bodySmall
        )
        ui.conditions.forEach { condition ->
            Text(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clickable { onConditionClick(condition) },
                text = condition.text,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.sp)
            )
        }

        Column {

            ui.options.forEach { option ->
                val updatedOption = option.copy(value = !option.value)

                Row(
                    modifier = Modifier
                        .clickable { onClick(ui, updatedOption) }
                        .padding(16.dp)) {
                    Checkbox(
                        modifier = Modifier.size(24.dp),
                        checked = option.value,
                        onCheckedChange = {
                            onClick(ui, updatedOption)
                        },
                        colors = CheckboxDefaults.colors(
                            checkmarkColor = MaterialTheme.colorScheme.background,
                            checkedColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Text(
                        text = option.label,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .weight(1f),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyMedium
                    )

                }
            }
        }
    }
}

@Composable
private fun SwitchComponent(
    modifier: Modifier = Modifier, ui: SwitchUi,
    onClick: (SwitchUi, String) -> Unit,
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            text = ui.label,
            color = if (ui.error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineSmall
        )
        FlowRow(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ui.options.forEach { opt ->
                Button(
                    modifier = Modifier.requiredHeight(38.dp),
                    onClick = { onClick(ui, opt) },
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (ui.selectedOption == opt) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text(
                        text = opt,
                        color = if (ui.selectedOption == opt) {
                            MaterialTheme.colorScheme.background
                        } else {
                            MaterialTheme.colorScheme.onBackground
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.1.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun CheckboxListComponent(
    modifier: Modifier = Modifier,
    ui: CheckboxListUi,
    onClick: (CheckboxListUi, ComponentOptionUi) -> Unit,
) {
    Column(modifier = modifier) {
        Text(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            text = ui.label,
            color = if (ui.error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineSmall
        )
        ui.options.forEach { opt ->
            val updatedOption = opt.copy(value = !opt.value)

            Row(
                modifier = Modifier
                    .clickable {
                        onClick(ui, updatedOption)
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = opt.label,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyMedium
                )

                Checkbox(
                    modifier = Modifier.size(24.dp),
                    checked = opt.value,
                    onCheckedChange = {
                        onClick(ui, updatedOption)
                    },
                    colors = CheckboxDefaults.colors(
                        checkmarkColor = MaterialTheme.colorScheme.background,
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.outline
                    )
                )

            }
        }
    }
}

@Composable
private fun ToggleListComponent(
    modifier: Modifier = Modifier,
    ui: ToggleListUi,
    onClick: (ToggleListUi, ComponentOptionUi) -> Unit,
) {
    Column(modifier = modifier) {
        Text(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            text = ui.label,
            color = if (ui.error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineSmall
        )
        ui.options.forEach { option ->
            Row(
                modifier = Modifier
                    .clickable {
                        onClick(ui, option)
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = option.label,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyMedium
                )

                VodovozRadioButton(
                    modifier = Modifier.padding(start = 16.dp),
                    selected = option.value,
                    onClick = {
                        onClick(ui, option)
                    }
                )
            }
        }
    }
}