package com.vodovoz.app.feature.profile.change_password.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vodovoz.app.design_system.composables.text_fields.VodovozTextFieldsColumn
import com.vodovoz.app.design_system.model.widgets.FieldUi

@Suppress("NonSkippableComposable")
@Composable
fun ChangePasswordBody(
    modifier: Modifier = Modifier,
    fields: List<FieldUi>,
    onFieldChange: (FieldUi, FieldUi) -> Unit,
    onUpdatePasswordClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        VodovozTextFieldsColumn(
            modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp),
            fields = fields,
            onFieldChange = { field, updatedField ->
                onFieldChange(field, updatedField)
            },
            onDone = {
                onUpdatePasswordClick()
            },
        )
    }

}