package com.m.vodovoz.feature.profile.change_password

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.VodovozLongPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.profile.change_password.model.ChangePasswordEvent
import com.m.vodovoz.feature.profile.change_password.model.ChangePasswordUiState
import com.m.vodovoz.ui.mvi.collectAsState
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChangePasswordEntry() = NavigationEntry<ChangePasswordViewModel> {
    val viewState by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val imeIsVisible = WindowInsets.isImeVisible
    DisposableEffect(imeIsVisible) {
        viewModel.tabManager.setTabVisibility(!imeIsVisible)
        onDispose {
            viewModel.tabManager.setTabVisibility(true)
        }
    }

    when (val uiState = viewState.uiState) {
        ChangePasswordUiState.ChangePassword -> {
            ChangePasswordScreen(
                viewModel = viewModel,
                viewState = viewState,
                snackbarHostState = snackbarHostState
            )
        }

        ChangePasswordUiState.Loading -> {
            LoadingPlaceholder()
        }

        is ChangePasswordUiState.Placeholder -> {
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
    }

    LifecycleEffect(snackbarHostState) {
        viewModel.events.collect { event ->
            when (event) {
                ChangePasswordEvent.GoBack -> navigator.goBack()
                is ChangePasswordEvent.ShowSnackbar -> {
                    launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar(event.message)
                    }
                }
            }
        }
    }
}
