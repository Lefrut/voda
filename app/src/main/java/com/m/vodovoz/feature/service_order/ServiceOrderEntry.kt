package com.m.vodovoz.feature.service_order

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.compose.LifecycleStartEffect
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.core.navigation.navigateToWebView
import com.m.vodovoz.feature.service_order.api.ServiceOrderNavKey
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.ui.mvi.collectEvents

@Composable
fun ServiceOrderEntry(navKey: ServiceOrderNavKey? = null) =
    NavigationEntry<ServiceOrderViewModel, ServiceOrderViewModel.Factory>(
        creationCallback = { factory -> factory.create(navKey) }
    ) {
    val view = LocalView.current

    LifecycleStartEffect(view) {
        viewModel.tabManager.changeTabVisibility(false)

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            viewModel.insetsState.consumeNavigationBarInsets(!imeVisible)
            insets
        }
        onStopOrDispose {
            ViewCompat.setOnApplyWindowInsetsListener(view, null)
            viewModel.insetsState.consumeNavigationBarInsets(true)
            viewModel.tabManager.changeTabVisibility(true)
        }
    }

    val viewState by viewModel.collectAsState()


    ServiceOrderScreen(
        viewModel = viewModel,
        viewState = viewState
    )


    viewModel.collectEvents { event ->
        when (event) {
            ServiceOrderViewModel.ServiceOrderEvent.GoBack -> {
                navigator.goBack()
            }

            is ServiceOrderViewModel.ServiceOrderEvent.GoToWebView -> {
                navigator.navigateToWebView(event.url, event.title)
            }
        }
    }
}
