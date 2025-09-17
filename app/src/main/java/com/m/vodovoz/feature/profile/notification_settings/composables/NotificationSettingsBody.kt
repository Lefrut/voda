package com.m.vodovoz.feature.profile.notification_settings.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import com.m.vodovoz.design_system.composables.swich.VodovozSwitch
import com.m.vodovoz.design_system.composables.text_fields.VodovozTextField
import com.m.vodovoz.design_system.model.SectionUi
import com.m.vodovoz.design_system.model.widgets.FieldUi
import com.m.vodovoz.design_system.model.widgets.SwitchUi
import com.m.vodovoz.design_system.model.widgets.WidgetUi

@Suppress("NonSkippableComposable")
@Composable
fun NotificationSettingsBody(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    header: String,
    sections: List<SectionUi<WidgetUi>>,
    onWidgetChange: (WidgetUi, WidgetUi) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp)
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        if(header.isNotBlank()){
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = header,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        sections.forEach { switchSection ->
            NotificationSectionColumn(
                section = switchSection,
                onWidgetChange = onWidgetChange
            )
        }
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

