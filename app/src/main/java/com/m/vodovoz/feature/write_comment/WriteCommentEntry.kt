package com.m.vodovoz.feature.write_comment

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.design_system.composables.placeholders.VodovozLongPlaceholder
import com.m.vodovoz.feature.write_comment.model.WriteCommentEvent
import com.m.vodovoz.feature.write_comment.model.WriteCommentUiState
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.ui.mvi.collectEvents

@Composable
fun WriteCommentEntry() = NavigationEntry<WriteCommentViewModel> {
    val viewState by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val pickImagesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uri -> viewModel.addUri(uri) }

    DisposableEffect(Unit) {
        val tabManager = viewModel.tabManager
        tabManager.changeTabVisibility(false)
        onDispose {
            tabManager.changeTabVisibility(true)
        }
    }

    when (val uiState = viewState.uiState) {
        WriteCommentUiState.Comment -> {
            WriteCommentScreen(
                viewModel = viewModel,
                viewState = viewState,
                snackbarHostState = snackbarHostState
            )
        }

        is WriteCommentUiState.Success -> {
            VodovozLongPlaceholder(
                data = uiState.placeholder,
                onButtonClick = { viewModel.navigateBack() },
                onCloseClick = { viewModel.navigateBack() }
            )
        }
    }

    viewModel.collectEvents { event ->
        when (event) {
            WriteCommentEvent.GoBack -> {
                navigator.goBack()
            }

            WriteCommentEvent.OpenImagePicker -> {
                pickImagesLauncher.launch(arrayOf("image/*"))
            }

            is WriteCommentEvent.SetRatedProductResult -> {
                navigator.previousBackStackEntry?.savedStateHandle?.set(
                    key = "ratedProductId",
                    value = event.productId
                )
            }

            is WriteCommentEvent.ShowSnackbar -> {
                snackbarHostState.showSnackbar(event.message)
            }
        }
    }
}
