package com.m.vodovoz.feature.about_product

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LifecycleStartEffect
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.feature.about_product.api.AboutProductNavKey
import com.m.vodovoz.feature.document_viewer.api.DocumentViewerNavKey
import com.m.vodovoz.core.navigation.navigateToProductAnalogs
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.about_product.model.AboutProductEvent
import com.m.vodovoz.ui.mvi.collectAsState

@Composable
fun AboutProductEntry(navKey: AboutProductNavKey? = null) =
    NavigationEntry<AboutProductViewModel, AboutProductViewModel.Factory>(
        creationCallback = { factory -> factory.create(navKey) }
    ) {
    val viewState by viewModel.collectAsState()

    LifecycleStartEffect(Unit) {
        viewModel.tabManager.setTabVisibility(false)
        onStopOrDispose {
            viewModel.tabManager.setTabVisibility(true)
        }
    }

    AboutProductScreen(
        viewModel = viewModel,
        viewState = viewState
    )

    LifecycleEffect {
        viewModel.listenCartUpdates()
    }

    LifecycleEffect {
        viewModel.events.collect { event ->
            when (event) {
                AboutProductEvent.GoBack -> {
                    navigator.goBack()
                }

                is AboutProductEvent.GoToDocumentViewer -> {
                    navigator.navigate(DocumentViewerNavKey(documentId = event.document))
                }

                is AboutProductEvent.GoToProductAnalogs -> {
                    navigator.navigateToProductAnalogs(event.productId)
                }
            }
        }
    }
}
