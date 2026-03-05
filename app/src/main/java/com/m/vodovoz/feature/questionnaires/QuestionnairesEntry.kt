package com.m.vodovoz.feature.questionnaires

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LifecycleStartEffect
import com.m.vodovoz.R
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.core.navigation.navigateToWebView
import com.m.vodovoz.design_system.composables.dialogs.VodovozDialog
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.VodovozLongPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.ui.mvi.collectAsState
import kotlinx.coroutines.launch

@Composable
fun QuestionnairesEntry() = NavigationEntry<QuestionnairesFlowViewModel> {
    val viewState by viewModel.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LifecycleStartEffect(Unit) {
        viewModel.tabManager.changeTabVisibility(false)
        onStopOrDispose {
            viewModel.tabManager.changeTabVisibility(true)
        }
    }

    when (val uiState = viewState.uiState) {
        QuestionnairesFlowViewModel.QuestionnairesUiState.Body -> {
            QuestionnairesScreen(
                viewModel = viewModel,
                viewState = viewState,
                scrollState = scrollState,
                snackbarHostState = snackbarHostState
            )
        }

        QuestionnairesFlowViewModel.QuestionnairesUiState.Loading -> {
            LoadingPlaceholder()
        }

        is QuestionnairesFlowViewModel.QuestionnairesUiState.Success -> {
            VodovozLongPlaceholder(
                data = uiState.placeholder,
                onCloseClick = {
                    viewModel.navigateBack()
                },
                onButtonClick = {
                    viewModel.navigateBack()
                }
            )
        }

        is QuestionnairesFlowViewModel.QuestionnairesUiState.Welcome -> {
            WelcomeQuestionnairesScreen(
                uiState = uiState,
                onWelcomeButtonClick = { btn ->
                    viewModel.activateWelcomeButton(btn)
                },
                onBackClick = {
                    viewModel.navigateBack()
                }
            )
        }

        QuestionnairesFlowViewModel.QuestionnairesUiState.Error -> {
            NetworkErrorPlaceholder { viewModel.fetchWelcomeDetails() }
        }
    }

    if (viewState.showCancelDialog) {
        VodovozDialog(
            title = stringResource(R.string.questionnaire_cancel_title),
            description = stringResource(R.string.questionnaire_cancel_description),
            acceptButtonText = stringResource(R.string.exit),
            cancelButtonText = stringResource(R.string.cancel),
            onDismiss = {
                viewModel.closeCancelDialog()
            },
            onAccept = {
                viewModel.closeCancelDialog()
                viewModel.fetchWelcomeDetails()
            }
        )
    }

    LifecycleEffect(snackbarHostState) {
        viewModel.events.collect { event ->
            when (event) {
                QuestionnairesFlowViewModel.QuestionnaireEvents.GoBack -> {
                    navController.popBackStack()
                }

                is QuestionnairesFlowViewModel.QuestionnaireEvents.GoToWebView -> {
                    navController.navigateToWebView(
                        event.url,
                        context.getString(R.string.space)
                    )
                }

                QuestionnairesFlowViewModel.QuestionnaireEvents.ScrollToTop -> {
                    launch { scrollState.animateScrollTo(0) }
                }

                is QuestionnairesFlowViewModel.QuestionnaireEvents.ShowToast -> {
                    launch {
                        snackbarHostState.showSnackbar(event.message)
                    }
                }
            }
        }
    }

    BackHandler {
        viewModel.navigateBack()
    }
}
