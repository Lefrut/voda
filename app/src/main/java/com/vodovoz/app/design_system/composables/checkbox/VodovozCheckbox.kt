package com.vodovoz.app.design_system.composables.checkbox

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vodovoz.app.design_system.composables.text.LinkedText
import com.vodovoz.app.design_system.model.widgets.CheckboxUi

@Composable
fun VodovozCheckbox(
    modifier: Modifier = Modifier,
    checkbox: CheckboxUi,
    onCheckboxClick: (CheckboxUi, CheckboxUi) -> Unit,
    onUrlClick: (url: String, title: String) -> Unit,
) {
    Row(modifier = modifier) {
        Checkbox(
            modifier = Modifier.size(24.dp),
            checked = checkbox.checked,
            onCheckedChange = { checked ->
                onCheckboxClick(
                    checkbox,
                    checkbox.copy(checked = checked)
                )
            },
            colors = CheckboxDefaults.colors(
                checkmarkColor = MaterialTheme.colorScheme.background,
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = if (checkbox.error) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.outline
                }
            ),
        )
        Spacer(modifier = Modifier.width(16.dp))

        LinkedText(
            modifier = Modifier.weight(1f),
            text = checkbox.name,
            onUrlClick = { url, urlIndex ->
                onUrlClick(url, checkbox.urlTitles.getOrElse(urlIndex) { "" })
            },
            color = if (checkbox.error) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onBackground
            }
        )
    }

}