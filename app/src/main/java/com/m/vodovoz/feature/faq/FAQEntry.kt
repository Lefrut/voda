package com.m.vodovoz.feature.faq

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LifecycleStartEffect
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.faq.api.FAQNavKey
import com.m.vodovoz.feature.faq.model.FAQEvent
import com.m.vodovoz.ui.mvi.collectAsState

@Composable
fun FAQEntry(navKey: FAQNavKey) =
    NavigationEntry<FAQViewModel, FAQViewModel.Factory>(
        creationCallback = { factory -> factory.create(navKey) }
    ) {
    val viewState by viewModel.collectAsState()

    LifecycleStartEffect(Unit) {
        viewModel.tabManager.setTabVisibility(false)
        onStopOrDispose {
            viewModel.tabManager.setTabVisibility(true)
        }
    }

    FAQScreen(viewModel = viewModel, viewState = viewState)

    LifecycleEffect {
        viewModel.events.collect { event ->
            when (event) {
                FAQEvent.GoBack -> navigator.goBack()
            }
        }
    }
}
