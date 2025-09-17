package com.m.vodovoz.design_system.composables.radio_button

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.m.vodovoz.design_system.composables.button.VodovozRadioButton
import com.m.vodovoz.design_system.model.widgets.RadioButtonGroupUi

@Composable
fun <T : Any> VodovozRadioButtonColumn(
    modifier: Modifier = Modifier,
    radioButtonGroup: RadioButtonGroupUi<T>,
    onOptionClick: (RadioButtonGroupUi<T>, RadioButtonGroupUi<T>) -> Unit,
) {
    Column(modifier = modifier.selectableGroup()) {
        val currentOption = radioButtonGroup.option

        radioButtonGroup.options.forEach { option ->
            Row(
                modifier = Modifier
                    .clickable { onOptionClick(radioButtonGroup, radioButtonGroup.copy(option = option)) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = option.name,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyMedium
                )

                VodovozRadioButton(
                    modifier = Modifier.padding(start = 16.dp),
                    selected = currentOption.value == option.value,
                    onClick = { onOptionClick(radioButtonGroup, radioButtonGroup.copy(option = option)) }
                )
            }

        }
    }
}