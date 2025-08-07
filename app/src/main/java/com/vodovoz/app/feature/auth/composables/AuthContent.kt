package com.vodovoz.app.feature.auth.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vodovoz.app.R
import com.vodovoz.app.design_system.composables.button.VodovozButtonsColumn
import com.vodovoz.app.design_system.composables.decoration.AgreementRow
import com.vodovoz.app.design_system.composables.text_fields.VodovozTextFieldsColumn
import com.vodovoz.app.design_system.composables.top_bar.VodovozTopBar
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.widgets.CheckboxUi
import com.vodovoz.app.design_system.model.widgets.FieldUi
import com.vodovoz.app.feature.auth.model.AuthDetailsUi

@Composable
fun AuthContent(
    modifier: Modifier = Modifier,
    authDetails: AuthDetailsUi,
    onBackClick: () -> Unit,
    onFieldChange: (FieldUi, FieldUi) -> Unit,
    onCheckboxChange: (CheckboxUi, CheckboxUi) -> Unit,
    onButtonClick: (ColorfulButtonUi) -> Unit,
    onHyperlinkClick: (url: String, urlIndex: Int) -> Unit,
    onForgotPasswordClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        VodovozTopBar(
            onBack = onBackClick,
            title = authDetails.title
        )

        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            if (authDetails.description.isNotEmpty()) {
                Text(
                    modifier = Modifier.padding(bottom = 24.dp),
                    text = authDetails.description,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (authDetails.fields.isNotEmpty()) {
                VodovozTextFieldsColumn(
                    fields = authDetails.fields,
                    onFieldChange = onFieldChange,
                    onDone = { },
                )
            }

            if (onForgotPasswordClick != null) {
                Text(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp)
                        .clip(MaterialTheme.shapes.small)
                        .clickable(onClick = onForgotPasswordClick),
                    text = stringResource(id = R.string.forgot_password),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }


            val checkboxes = authDetails.checkboxes
            if (checkboxes.isNotEmpty()) {
                Column(
                    modifier = Modifier.padding(top = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    checkboxes.forEach { checkbox ->
                        key(checkbox.id) {
                            AgreementRow(
                                checked = checkbox.checked,
                                htmlText = checkbox.name,
                                onCheckedChange = {
                                    onCheckboxChange(checkbox, checkbox.copy(checked = it))
                                },
                                onUrlClick = onHyperlinkClick
                            )
                        }
                    }
                }
            }

            VodovozButtonsColumn(
                modifier = Modifier.padding(vertical = 24.dp),
                buttons = authDetails.buttons,
                onButtonClick = onButtonClick
            )
        }
    }

}