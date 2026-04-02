package com.m.vodovoz.feature.buy_certificate

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.m.vodovoz.R
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.core.navigation.navigateToFAQ
import com.m.vodovoz.core.navigation.navigateToProfile
import com.m.vodovoz.core.navigation.navigateToWebView
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.VodovozLongPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.util.extensions.openUrl
import kotlinx.coroutines.launch

@Composable
fun BuyCertificateEntry() = NavigationEntry<BuyCertificateViewModel> {
    val viewState by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    when (val uiState = viewState.uiState) {
        BuyCertificateViewModel.BuyCertificateUiState.Body -> {
            BuyCertificateScreen(
                viewModel = viewModel,
                viewState = viewState,
                snackbarHostState = snackbarHostState
            )
        }

        BuyCertificateViewModel.BuyCertificateUiState.Error -> {
            NetworkErrorPlaceholder {
                viewModel.fetchBuyCertificateDetails()
            }
        }

        BuyCertificateViewModel.BuyCertificateUiState.Loading -> {
            LoadingPlaceholder()
        }

        is BuyCertificateViewModel.BuyCertificateUiState.Success -> {
            VodovozLongPlaceholder(
                data = uiState.placeholder,
                onCloseClick = { viewModel.navigateBack() },
                onButtonClick = { viewModel.pay() }
            )
        }
    }

    LifecycleEffect(snackbarHostState) {
        viewModel.events.collect { event ->
            when (event) {
                BuyCertificateViewModel.BuyCertificateEvents.GoBack -> {
                    navigator.goBack()
                }

                is BuyCertificateViewModel.BuyCertificateEvents.OpenLink -> Unit

                is BuyCertificateViewModel.BuyCertificateEvents.GoToFAQ -> {
                    navigator.navigateToFAQ(event.faq)
                }

                BuyCertificateViewModel.BuyCertificateEvents.GoToProfile -> {
                    navigator.navigateToProfile(viewModel.tabManager)
                }

                is BuyCertificateViewModel.BuyCertificateEvents.GoToWebView -> {
                    navigator.navigateToWebView(
                        event.url,
                        context.getString(R.string.space)
                    )
                }

                is BuyCertificateViewModel.BuyCertificateEvents.OpenUrl -> {
                    context.openUrl(event.url)
                }

                is BuyCertificateViewModel.BuyCertificateEvents.ShowToast -> {
                    launch { snackbarHostState.showSnackbar(event.message) }
                }
            }
        }
    }
}
