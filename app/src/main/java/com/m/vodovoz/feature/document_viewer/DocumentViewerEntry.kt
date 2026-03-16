package com.m.vodovoz.feature.document_viewer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LifecycleStartEffect
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.design_system.effects.AppearanceSystemBarsEffect
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.document_viewer.api.DocumentViewerNavKey
import com.m.vodovoz.feature.document_viewer.model.DocumentViewerEvent
import com.m.vodovoz.ui.mvi.collectAsState

@Composable
fun DocumentViewerEntry(navKey: DocumentViewerNavKey? = null) =
    NavigationEntry<DocumentViewerViewModel, DocumentViewerViewModel.Factory>(
        creationCallback = { factory -> factory.create(navKey) }
    ) {
    val viewState by viewModel.collectAsState()

    LifecycleStartEffect(Unit) {
        viewModel.tabManager.changeTabVisibility(false)
        onStopOrDispose {
            viewModel.tabManager.changeTabVisibility(true)
        }
    }

    AppearanceSystemBarsEffect(
        lightStatusBar = true,
        lightNavigationBar = true
    )

    DocumentViewerScreen(
        viewModel = viewModel,
        viewState = viewState
    )

    LifecycleEffect {
        viewModel.events.collect { event ->
            when (event) {
                DocumentViewerEvent.GoBack -> {
                    navigator.goBack()
                }
            }
        }
    }
}
