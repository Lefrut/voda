package com.m.vodovoz.feature.certificate_activation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.m.vodovoz.R
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.core.navigation.navigateToWebView
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.certificate_activation.composables.CertificateActivatedPlaceholder
import com.m.vodovoz.feature.certificate_activation.model.CertificateActivationEvent
import com.m.vodovoz.feature.certificate_activation.model.CertificateActivationUiState
import com.m.vodovoz.ui.mvi.collectAsState

@Composable
fun CertificateActivationEntry() = NavigationEntry<CertificateActivationViewModel> {
    val context = LocalContext.current
    val viewState by viewModel.collectAsState()

    when (val uiState = viewState.uiState) {
        is CertificateActivationUiState.CertificateActivated -> {
            CertificateActivatedPlaceholder(
                message = uiState.message,
                onCloseClick = {
                    viewModel.navigateBack()
                },
                onOkClick = {
                    viewModel.navigateBack()
                }
            )
        }

        CertificateActivationUiState.Error -> {
            NetworkErrorPlaceholder {
                viewModel.fetchCertificateActivationDetails()
            }
        }

        else -> {
            CertificateActivationScreen(
                viewModel = viewModel,
                viewState = viewState
            )
        }
    }

    LifecycleEffect {
        viewModel.events.collect { event ->
            when (event) {
                CertificateActivationEvent.GoBack -> {
                    navigator.goBack()
                }

                is CertificateActivationEvent.GoToWebView -> {
                    navigator.navigateToWebView(
                        event.url,
                        context.getString(R.string.space)
                    )
                }
            }
        }
    }
}
