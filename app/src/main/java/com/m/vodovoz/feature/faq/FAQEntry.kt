package com.m.vodovoz.feature.faq

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LifecycleStartEffect
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.faq.model.FAQEvent
import com.m.vodovoz.ui.mvi.collectAsState

@Composable
fun FAQEntry() = NavigationEntry<FAQViewModel> {
    val viewState by viewModel.collectAsState()

    LifecycleStartEffect(Unit) {
        viewModel.tabManager.changeTabVisibility(false)
        onStopOrDispose {
            viewModel.tabManager.changeTabVisibility(true)
        }
    }

    FAQScreen(viewModel = viewModel, viewState = viewState)

    LifecycleEffect {
        viewModel.events.collect { event ->
            when (event) {
                FAQEvent.GoBack -> navController.popBackStack()
            }
        }
    }
}
