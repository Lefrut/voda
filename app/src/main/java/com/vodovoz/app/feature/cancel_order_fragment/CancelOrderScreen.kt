package com.vodovoz.app.feature.cancel_order_fragment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vodovoz.app.design_system.composables.button.VodovozButtonsColumn
import com.vodovoz.app.design_system.composables.text_fields.VodovozTextField
import com.vodovoz.app.design_system.composables.top_bar.ClosingTopBar
import com.vodovoz.app.feature.cancel_order_fragment.model.CancelOrderState

@Composable
fun CancelOrderScreen(viewModel: CancelOrderViewModel, viewState: CancelOrderState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        ClosingTopBar(title = viewState.title) {
            viewModel.navigateBack()
        }
        Text(
            modifier = Modifier.padding(
                top = 8.dp,
                start = 16.dp,
                end = 16.dp
            ),
            text = AnnotatedString.fromHtml(viewState.description),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = AnnotatedString.fromHtml(viewState.warningText),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(
                start = 10.dp,
                end = 20.dp,
                top = 16.dp,
                bottom = 24.dp
            )
        )

        viewState.checkboxesNames.forEach { checkboxName ->
            CheckBoxRow(
                name = checkboxName,
                selected = viewState.currentCheckboxName == checkboxName,
                onCheckedChange = { name ->
                    viewModel.changeCurrentCheckbox(name)
                }
            )
        }

        viewState.commentField?.let {
            VodovozTextField(
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp
                ),
                field = viewState.commentField,
                onFieldChange = { field, updatedField ->
                    viewModel.changeField(field, updatedField)
                },
                maxLines = 3,
                minLines = 2
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        VodovozButtonsColumn(
            modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp),
            buttons = listOf(viewState.button),
            onButtonClick = { btn ->
                viewModel.cancelOrder(btn)
            }
        )

    }
}

@Composable
private fun CheckBoxRow(
    modifier: Modifier = Modifier,
    name: String,
    selected: Boolean,
    onCheckedChange: (String) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onCheckedChange(name) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            text = name,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Checkbox(
            checked = selected,
            onCheckedChange = {
                onCheckedChange(name)
            },
            colors = CheckboxDefaults.colors(
                checkmarkColor = MaterialTheme.colorScheme.background,
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}
