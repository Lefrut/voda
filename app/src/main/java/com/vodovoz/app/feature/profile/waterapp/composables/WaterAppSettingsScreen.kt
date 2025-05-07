package com.vodovoz.app.feature.profile.waterapp.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vodovoz.app.R
import com.vodovoz.app.design_system.composables.button.VodovozButton
import com.vodovoz.app.design_system.composables.top_bar.ClosingTopBar
import com.vodovoz.app.feature.profile.waterapp.WaterAppHelper
import com.vodovoz.app.feature.profile.waterapp.model.ReminderIntervalUi

@Suppress("NonSkippableComposable")
@Composable
fun WaterAppSettingsScreen(
    modifier: Modifier = Modifier,
    onCloseClick: () -> Unit,
    intervals: List<ReminderIntervalUi>,
) {
    Column(modifier = modifier.fillMaxSize()) {
        ClosingTopBar(title = stringResource(id = R.string.settings), onCloseClick = onCloseClick)
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.notifications),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall
            )
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(id = R.string.water_reminder_message),
                    color = MaterialTheme.colorScheme.surfaceTint,
                    style = MaterialTheme.typography.bodySmall
                )
                Switch(
                    modifier = Modifier.padding(start = 16.dp),
                    checked = true,
                    onCheckedChange = {},
                    colors = SwitchDefaults.colors()
                )
            }

            Text(
                modifier = Modifier.padding(top = 32.dp),
                text = stringResource(R.string.reminder_interval),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall
            )

            FlowRow {
                intervals.forEach { interval ->

                }
            }

            Spacer(modifier = Modifier.weight(1f))

            VodovozButton(
                modifier = Modifier.padding(vertical = 24.dp),
                text = stringResource(id = R.string.save),
                onClick = {

                }
            )
        }
    }
}


@Composable
private fun ReminderCard(modifier: Modifier = Modifier, reminderIntervalUi: ReminderIntervalUi) {
    val showHorse = WaterAppHelper.shouldDisplayIntervalAsHours(reminderIntervalUi.minutes)
    val value = WaterAppHelper.formatReminderMinutes(reminderIntervalUi.minutes)
    val contentColor = if (reminderIntervalUi.selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val containerColor =if (reminderIntervalUi.selected) {
        MaterialTheme.colorScheme.background
    } else {
        MaterialTheme.colorScheme.primary
    }



    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(color = containerColor),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = value, color = contentColor)
    }
}