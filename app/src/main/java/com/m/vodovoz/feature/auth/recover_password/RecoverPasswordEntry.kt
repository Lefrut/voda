package com.m.vodovoz.feature.auth.recover_password

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.core.navigation.navigateToWebView
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.VodovozLongPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.auth.recover_password.model.RecoverPasswordEvent
import com.m.vodovoz.feature.auth.recover_password.model.RecoverPasswordUiState
import com.m.vodovoz.ui.mvi.collectAsState

@Composable
fun RecoverPasswordEntry() = NavigationEntry<RecoverPasswordViewModel> {
    val viewState by viewModel.collectAsState()

    when (val uiState = viewState.uiState) {
        RecoverPasswordUiState.Body -> {
            RecoverPasswordScreen(viewModel = viewModel, viewState = viewState)
        }

        RecoverPasswordUiState.Error -> {
            NetworkErrorPlaceholder {
                viewModel.fetchRecoverPasswordDetails()
            }
        }

        RecoverPasswordUiState.Loading -> {
            LoadingPlaceholder()
        }

        is RecoverPasswordUiState.Success -> {
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

    LifecycleEffect {
        viewModel.events.collect { event ->
            when (event) {
                RecoverPasswordEvent.GoBack -> {
                    navController.popBackStack()
                }

                is RecoverPasswordEvent.GoToWebView -> {
                    navController.navigateToWebView(event.url, event.title)
                }
            }
        }
    }
}
