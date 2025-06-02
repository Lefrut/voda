package com.vodovoz.app.design_system.composables.swich

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.model.widgets.SwitchUi


@Composable
fun VodovozSwitch(
    modifier: Modifier = Modifier,
    switch: SwitchUi,
    onSwitchChange: (SwitchUi, SwitchUi) -> Unit,
) {
    Row(
        modifier = modifier
            .clickable { onSwitchChange(switch, switch.copy(value = !switch.value)) }
            .heightIn(64.dp)
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = switch.name,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium
        )

        Switch(
            modifier = Modifier
                .padding(start = 16.dp)
                .requiredHeight(32.dp),
            checked = switch.value,
            onCheckedChange = {
                onSwitchChange(switch, switch.copy(value = !switch.value))
            },
            colors = SwitchDefaults.vodovozColors()
        )
    }
}

@Preview
@Composable
private fun VodovozSwitchPreview() {
    VodovozTheme {

    }
}

@Composable
fun SwitchDefaults.vodovozColors() = colors(
    uncheckedTrackColor = Color.Transparent,
    checkedTrackColor = MaterialTheme.colorScheme.primary,
    uncheckedThumbColor = MaterialTheme.colorScheme.primary,
    checkedThumbColor = MaterialTheme.colorScheme.background,
    checkedBorderColor = MaterialTheme.colorScheme.primary,
    uncheckedBorderColor = MaterialTheme.colorScheme.primary
)
