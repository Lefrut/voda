package com.m.vodovoz.common.webview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.m.vodovoz.common.webview.model.WebViewEvents
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.ui.mvi.collectAsState

@Composable
fun WebViewEntry() = NavigationEntry<WebViewViewModel> {
    val viewState by viewModel.collectAsState()

    WebViewScreen(
        viewModel = viewModel,
        viewState = viewState
    )

    LifecycleEffect {
        viewModel.events.collect { event ->
            when (event) {
                WebViewEvents.GoBack -> navigator.goBack()
            }
        }
    }
}
