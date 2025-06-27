package com.vodovoz.app.design_system.composables.checkbox

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vodovoz.app.design_system.model.widgets.SingleCheckboxGroup

@Composable
fun <T : Any> VodovozSingleCheckboxColumn(
    modifier: Modifier = Modifier,
    singleCheckboxGroup: SingleCheckboxGroup<T>,
    onCheckboxClick: (SingleCheckboxGroup<T>, SingleCheckboxGroup<T>) -> Unit,
) {
    Column(modifier = modifier.selectableGroup()) {
        val currentOption = singleCheckboxGroup.checkbox

        singleCheckboxGroup.checkboxes.forEach { option ->

            val selected = option == currentOption

            Row(
                modifier = Modifier
                    .clickable { onCheckboxClick(singleCheckboxGroup, singleCheckboxGroup.copy(checkbox = if(selected) null else option)) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = option.name,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyMedium
                )

                Checkbox(
                    modifier = Modifier.padding(start = 16.dp).size(24.dp),
                    checked = selected,
                    onCheckedChange = {
                        onCheckboxClick(singleCheckboxGroup, singleCheckboxGroup.copy(checkbox = if(selected) null else option))
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