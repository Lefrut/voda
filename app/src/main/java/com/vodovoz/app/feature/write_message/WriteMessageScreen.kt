package com.vodovoz.app.feature.write_message

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vodovoz.app.design_system.composables.button.VodovozButtonsColumn
import com.vodovoz.app.design_system.composables.placeholders.LoadingPlaceholder
import com.vodovoz.app.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.vodovoz.app.design_system.composables.placeholders.VodovozLongPlaceholder
import com.vodovoz.app.design_system.composables.top_bar.VodovozTopBar
import com.vodovoz.app.feature.write_message.composables.WriteMessageBody
import com.vodovoz.app.feature.write_message.model.WriteMessageState
import com.vodovoz.app.feature.write_message.model.WriteMessageUiState

@Composable
fun WriteMessageScreen(
    viewModel: WriteMessageViewModel,
    viewState: WriteMessageState,
) {
    val buttons = listOf(viewState.button)

    Scaffold(
        bottomBar = {
            if (viewState.uiState is WriteMessageUiState.Body) {
                VodovozButtonsColumn(
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 24.dp
                    ),
                    buttons = buttons
                ) {
                    viewModel.sendMessage()
                }
            }
        },
        topBar = {
            VodovozTopBar(
                title = viewState.title,
                onBack = {
                    viewModel.navigateBack()
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (val uiState = viewState.uiState) {
                WriteMessageUiState.Body -> {
                    WriteMessageBody(
                        fields = viewState.fields,
                        onFieldChange = { field, updatedField ->
                            viewModel.changeField(field, updatedField)
                        }
                    )
                }

                WriteMessageUiState.Error -> {
                    NetworkErrorPlaceholder {
                        viewModel.fetchWriteMessageDetails()
                    }
                }

                WriteMessageUiState.Loading -> {
                    LoadingPlaceholder()
                }

                is WriteMessageUiState.Success -> {
                    VodovozLongPlaceholder(
                        data = uiState.placeholder,
                        onButtonClick = {
                            viewModel.navigateBack()
                        },
                        onCloseClick = {
                            viewModel.navigateBack()
                        }
                    )
                }
            }
        }
    }
}

