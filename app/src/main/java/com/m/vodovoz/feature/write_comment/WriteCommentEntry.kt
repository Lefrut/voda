package com.m.vodovoz.feature.write_comment

import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.design_system.composables.placeholders.VodovozLongPlaceholder
import com.m.vodovoz.feature.home.HomeFlowViewModel
import com.m.vodovoz.feature.wait_feedback_products.WaitFeedbackProductsViewModel
import com.m.vodovoz.feature.write_comment.api.WriteCommentNavKey
import com.m.vodovoz.feature.write_comment.model.WriteCommentEvent
import com.m.vodovoz.feature.write_comment.model.WriteCommentUiState
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.ui.mvi.collectEvents

@Composable
fun WriteCommentEntry(navKey: WriteCommentNavKey? = null) =
    NavigationEntry<WriteCommentViewModel, WriteCommentViewModel.Factory>(
        creationCallback = { factory -> factory.create(navKey) }
    ) {
    val activityOwner = LocalActivity.current as? ViewModelStoreOwner
    val homeViewModel = when (navKey?.source) {
        WriteCommentNavKey.Source.Home -> activityOwner?.let { hiltViewModel<HomeFlowViewModel>(it) }
        else -> null
    }
    val waitFeedbackProductsViewModel = when (navKey?.source) {
        WriteCommentNavKey.Source.WaitFeedbackProducts -> viewModel(modelClass = WaitFeedbackProductsViewModel::class)
        else -> null
    }
    val viewState by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val pickImagesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uri -> viewModel.addUri(uri) }

    DisposableEffect(Unit) {
        val tabManager = viewModel.tabManager
        tabManager.setTabVisibility(false)
        onDispose {
            tabManager.setTabVisibility(true)
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
                homeViewModel?.removeUnratedProduct(event.productId)
                waitFeedbackProductsViewModel?.removeProduct(event.productId)
            }

            is WriteCommentEvent.ShowSnackbar -> {
                snackbarHostState.showSnackbar(event.message)
            }
        }
    }
}
