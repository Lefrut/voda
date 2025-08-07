package com.vodovoz.app.feature.auth.recover_password.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vodovoz.app.design_system.composables.button.VodovozButtonsColumn
import com.vodovoz.app.design_system.composables.decoration.AgreementRow
import com.vodovoz.app.design_system.composables.text_fields.VodovozTextFieldsColumn
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.widgets.FieldUi

@Suppress("NonSkippableComposable")
@Composable
fun RecoverPasswordBody(
    modifier: Modifier = Modifier,
    description: String,
    agreementHtml: String,
    agreementChecked: Boolean,
    fields: List<FieldUi>,
    buttons: List<ColorfulButtonUi>,
    onFieldChange: (FieldUi, FieldUi) -> Unit,
    onButtonClick: (ColorfulButtonUi) -> Unit,
    onHyperlinkClick: (String, Int) -> Unit,
    onAgreementCheck: (Boolean) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (description.isNotEmpty()) {
            Text(
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = 8.dp),
                text = description,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodySmall
            )
        }
        VodovozTextFieldsColumn(
            modifier = Modifier.padding(top = 24.dp),
            fields = fields,
            onFieldChange = onFieldChange,
            onDone = {},
        )

        AgreementRow(
            modifier = Modifier.padding(top = 24.dp),
            checked = agreementChecked,
            htmlText = agreementHtml,
            onCheckedChange = onAgreementCheck,
            onUrlClick = onHyperlinkClick
        )

        Spacer(modifier = Modifier.weight(1f))

        VodovozButtonsColumn(
            buttons = buttons,
            onButtonClick = onButtonClick,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}