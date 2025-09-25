package com.m.vodovoz.feature.order_question

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.m.vodovoz.design_system.composables.button.VodovozButtonsColumn
import com.m.vodovoz.design_system.composables.snackbar.VodovozSnackbarHost
import com.m.vodovoz.design_system.composables.text_fields.VodovozTextFieldsColumn
import com.m.vodovoz.design_system.composables.top_bar.ClosingTopBar
import com.m.vodovoz.feature.order_question.model.OrderQuestionState

@Composable
fun OrderQuestionScreen(
    viewModel: OrderQuestionViewModel,
    viewState: OrderQuestionState,
    snackbarHostState: SnackbarHostState,
) {
    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
        snackbarHost = {
            VodovozSnackbarHost(hostState = snackbarHostState)
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            ClosingTopBar(
                title = viewState.title,
                onCloseClick = { viewModel.navigateBack() }
            )

            if (viewState.description.isNotBlank()) {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = viewState.description,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            VodovozTextFieldsColumn(
                modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp),
                fields = viewState.fields,
                onFieldChange = { field, updatedField ->
                    viewModel.changeField(field, updatedField)
                },
            )

            Spacer(modifier = Modifier.weight(1f))

            val btn = viewState.button
            VodovozButtonsColumn(
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 24.dp
                ),
                buttons = listOf(btn)
            ) {
                viewModel.sendMessage()
            }
        }
    }
}