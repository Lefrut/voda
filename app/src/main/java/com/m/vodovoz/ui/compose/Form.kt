package com.m.vodovoz.ui.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.m.vodovoz.design_system.composables.button.VodovozButtonsColumn
import com.m.vodovoz.design_system.composables.checkbox.VodovozCheckbox
import com.m.vodovoz.design_system.composables.scaffold.VodovozScaffold
import com.m.vodovoz.design_system.composables.snackbar.VodovozSnackbarHost
import com.m.vodovoz.design_system.composables.text_fields.VodovozTextFieldsColumn
import com.m.vodovoz.design_system.composables.top_bar.ClosingTopBar
import com.m.vodovoz.design_system.composables.top_bar.VodovozTopBar
import com.m.vodovoz.design_system.model.widgets.FieldUi
import com.m.vodovoz.feature.preorder.model.FormUi

@Composable
fun VodovozForm(
    modifier: Modifier = Modifier,
    form: FormUi,
    topBarStyle: FormTopBarStyle = FormTopBarStyle.Arrow,
    enableSpace: Boolean = true,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onCloseClick: () -> Unit,
    onFieldValueChange: (FieldUi, String) -> Unit,
    onCheckboxClick: (Boolean) -> Unit,
    onUrlClick: (url: String, title: String) -> Unit,
    onButtonClick: () -> Unit,
) {
    VodovozScaffold(
        modifier = modifier,
        topBar = {
            when (topBarStyle) {
                FormTopBarStyle.Arrow -> VodovozTopBar(
                    title = form.title,
                    onBack = onCloseClick
                )

                FormTopBarStyle.Cross -> ClosingTopBar(
                    title = form.title,
                    onCloseClick = onCloseClick
                )
            }

        },
        snackbarHost = {
            VodovozSnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        FormContent(
            modifier = Modifier.padding(top = paddingValues.calculateTopPadding()),
            form = form,
            enableSpace = enableSpace,
            contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding()),
            onFieldValueChange = onFieldValueChange,
            onCheckboxClick = onCheckboxClick,
            onUrlClick = onUrlClick,
            onButtonClick = onButtonClick
        )
    }
}

@Composable
private fun FormContent(
    modifier: Modifier = Modifier,
    form: FormUi,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    enableSpace: Boolean = true,
    onFieldValueChange: (FieldUi, String) -> Unit,
    onCheckboxClick: (Boolean) -> Unit,
    onUrlClick: (url: String, title: String) -> Unit,
    onButtonClick: () -> Unit,
) = with(form) {

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(contentPadding)
            .padding(top = 8.dp)
    ) {
        if (description.isNotBlank()) {
            Text(
                modifier = Modifier.padding(
                    bottom = 24.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                text = description,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodySmall
            )
        }

        VodovozTextFieldsColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            fields = fields,
            onFieldChange = { field, updatedField ->
                onFieldValueChange(field, updatedField.value)
            },
            onDone = {},
        )

        checkbox?.let {
            VodovozCheckbox(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                checkbox = checkbox,
                onCheckboxClick = { _, updatedCheckbox ->
                    onCheckboxClick(updatedCheckbox.checked)
                },
                onUrlClick = onUrlClick
            )
        }

        if (enableSpace) {
            Spacer(modifier = Modifier.weight(1f))
        }

        VodovozButtonsColumn(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
            buttons = listOf(form.button),
            onButtonClick = { onButtonClick() }
        )
    }

}


data object VodovozFormDefaults {


}

@Immutable
enum class FormTopBarStyle {
    Arrow, Cross
}