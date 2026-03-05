package com.m.vodovoz.feature.order_question

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.VodovozLongPlaceholder
import com.m.vodovoz.feature.order_question.model.OrderQuestionEvent
import com.m.vodovoz.feature.order_question.model.OrderQuestionUiState
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.ui.mvi.collectEvents

@Composable
fun OrderQuestionEntry() = NavigationEntry<OrderQuestionViewModel> {
    val viewState by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    DisposableEffect(Unit) {
        viewModel.tabManager.changeTabVisibility(false)
        onDispose {
            viewModel.tabManager.changeTabVisibility(true)
        }
    }

    when (val uiState = viewState.uiState) {
        OrderQuestionUiState.Error -> {
            NetworkErrorPlaceholder {
                viewModel.fetchOrderQuestionDetails()
            }
        }

        OrderQuestionUiState.Loading -> {
            LoadingPlaceholder()
        }

        OrderQuestionUiState.Fields -> {
            OrderQuestionScreen(
                viewModel = viewModel,
                viewState = viewState,
                snackbarHostState = snackbarHostState
            )
        }

        is OrderQuestionUiState.Success -> {
            VodovozLongPlaceholder(
                data = uiState.placeholderData,
                onCloseClick = {
                    viewModel.navigateBack()
                },
                onButtonClick = {
                    viewModel.navigateBack()
                }
            )
        }
    }

    viewModel.collectEvents { event ->
        when (event) {
            OrderQuestionEvent.GoBack -> {
                navController.popBackStack()
            }

            is OrderQuestionEvent.ShowToast -> {
                snackbarHostState.showSnackbar(event.message)
            }
        }
    }
}
