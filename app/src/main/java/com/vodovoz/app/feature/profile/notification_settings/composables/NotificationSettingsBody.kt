package com.vodovoz.app.feature.profile.notification_settings.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.dp
import com.vodovoz.app.design_system.composables.button.VodovozButtonsColumn
import com.vodovoz.app.design_system.composables.swich.VodovozSwitch
import com.vodovoz.app.design_system.composables.text_fields.VodovozTextField
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.SectionUi
import com.vodovoz.app.design_system.model.widgets.FieldUi
import com.vodovoz.app.design_system.model.widgets.SwitchUi
import com.vodovoz.app.design_system.model.widgets.WidgetUi

@Suppress("NonSkippableComposable")
@Composable
fun NotificationSettingsBody(
    modifier: Modifier = Modifier,
    header: String,
    sections: List<SectionUi<WidgetUi>>,
    button: ColorfulButtonUi,
    onWidgetChange: (WidgetUi, WidgetUi) -> Unit,
    onSaveClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = header,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        sections.forEach { switchSection ->
            NotificationSectionColumn(
                section = switchSection,
                onWidgetChange = onWidgetChange
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        VodovozButtonsColumn(
            modifier = Modifier.padding(
                top = 8.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = 24.dp
            ),
            buttons = listOf(button),
            onButtonClick = { onSaveClick() }
        )
    }
}

@Composable
fun NotificationSectionColumn(
    modifier: Modifier = Modifier,
    section: SectionUi<WidgetUi>,
    onWidgetChange: (WidgetUi, WidgetUi) -> Unit,
) {


    Column(modifier = modifier) {
        if (section.title.isNotEmpty())
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = AnnotatedString.fromHtml(section.title.trim()),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium
            )

        section.items.forEachIndexed { index, widget ->
            if (index == 0) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            when (widget) {
                is FieldUi -> {
                    VodovozTextField(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        field = widget,
                        onFieldChange = onWidgetChange
                    )
                }

                is SwitchUi -> {
                    VodovozSwitch(
                        switch = widget,
                        onSwitchChange = onWidgetChange
                    )
                }
                else ->{}
            }

            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

