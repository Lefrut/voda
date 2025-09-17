package com.m.vodovoz.feature.write_message

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.m.vodovoz.design_system.composables.button.VodovozButtonsColumn
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.VodovozLongPlaceholder
import com.m.vodovoz.design_system.composables.snackbar.VodovozSnackbarHost
import com.m.vodovoz.design_system.composables.top_bar.VodovozTopBar
import com.m.vodovoz.feature.write_message.composables.WriteMessageBody
import com.m.vodovoz.feature.write_message.model.WriteMessageState
import com.m.vodovoz.feature.write_message.model.WriteMessageUiState
import com.m.vodovoz.ui.compose.VodovozForm

@Composable
fun WriteMessageScreen(
    viewModel: WriteMessageViewModel,
    viewState: WriteMessageState,
    snackbarHostState: SnackbarHostState,
) {

    Box(modifier = Modifier.systemBarsPadding()) {
        when (val uiState = viewState.uiState) {
            WriteMessageUiState.Body -> {
                VodovozForm(
                    form = viewState.form,
                    onCloseClick = viewModel::navigateBack,
                    snackbarHostState = snackbarHostState,
                    onFieldValueChange = viewModel::changeFieldValue,
                    onCheckboxClick = viewModel::changeCheckbox,
                    onUrlClick = viewModel::navigateToWebView,
                    onButtonClick = viewModel::sendMessage
                )
            }

            WriteMessageUiState.Error -> {
                NetworkErrorPlaceholder { viewModel.fetchWriteMessageDetails() }
            }

            WriteMessageUiState.Loading -> {
                LoadingPlaceholder()
            }

            is WriteMessageUiState.Success -> {
                VodovozLongPlaceholder(
                    data = uiState.placeholder,
                    onButtonClick = viewModel::navigateBack,
                    onCloseClick = viewModel::navigateBack
                )

            }
        }

    }
}

