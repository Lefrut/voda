package com.m.vodovoz.feature.categories

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LifecycleStartEffect
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.categories.api.CategoriesNavKey
import com.m.vodovoz.feature.categories.model.CategoriesEvent
import com.m.vodovoz.ui.mvi.collectAsState

@Composable
fun CategoriesEntry(navKey: CategoriesNavKey? = null) =
    NavigationEntry<CategoriesViewModel, CategoriesViewModel.Factory>(
        creationCallback = { factory -> factory.create(navKey) }
    ) {
    val viewState by viewModel.collectAsState()

    LifecycleStartEffect(Unit) {
        viewModel.tabManager.setTabVisibility(false)
        onStopOrDispose {
            viewModel.tabManager.setTabVisibility(true)
        }
    }

    CategoriesScreen(viewModel = viewModel, viewState = viewState)

    LifecycleEffect {
        viewModel.events.collect { event ->
            when (event) {
                is CategoriesEvent.GoBackWithArguments -> {
                    navigator.previousBackStackEntry?.savedStateHandle?.set(
                        "category",
                        event.currentCategory
                    )
                    navigator.goBack()
                }

                CategoriesEvent.GoBack -> {
                    navigator.goBack()
                }
            }
        }
    }
}
