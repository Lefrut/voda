package com.vodovoz.app.feature.profile.notificationsettings.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vodovoz.app.design_system.composables.swich.vodovozColors
import com.vodovoz.app.design_system.composables.text_fields.VodovozTextField
import com.vodovoz.app.feature.preorder.model.FieldUi
import com.vodovoz.app.feature.profile.notificationsettings.model.SwitchSectionUi
import com.vodovoz.app.feature.profile.notificationsettings.model.SwitchUi

@Composable
fun NotificationSettingsBody(
    modifier: Modifier = Modifier,
    switchSections: List<SwitchSectionUi> = emptyList(),
    onSwitchChange: (SwitchUi, Boolean) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        switchSections.forEach { switchSection ->
            SwitchSectionItem(
                data = switchSection,
                onSwitchChange = onSwitchChange
            )
        }
    }
}

@Composable
fun SwitchSectionItem(
    modifier: Modifier = Modifier,
    data: SwitchSectionUi,
    onSwitchChange: (SwitchUi, Boolean) -> Unit,
) {
    Column(modifier = modifier) {
        if (data.title.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = data.title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall
            )
        }

        if (data.description.isNotEmpty()) {

            Text(
                modifier = Modifier.padding(top = 4.dp, start = 16.dp, end = 16.dp),
                text = data.description,
                color = MaterialTheme.colorScheme.surfaceTint,
                style = MaterialTheme.typography.bodySmall
            )
        }


        data.switches.forEachIndexed { index, switch ->
            if (index == 0) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Row(
                modifier = Modifier
                    .heightIn(64.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
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
                    checked = switch.checked,
                    onCheckedChange = {
                        onSwitchChange(switch, it)
                    },
                    colors = SwitchDefaults.vodovozColors()
                )
            }

            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
fun PhoneItem(
    title: String,
    field: FieldUi,
    onFieldChange: (FieldUi, FieldUi) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        if (title.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(bottom = 8.dp),
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall
            )
        }
        VodovozTextField(
            field = field, onFieldChange = onFieldChange
        )
    }

}

