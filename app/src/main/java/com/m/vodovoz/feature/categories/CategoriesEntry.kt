package com.m.vodovoz.feature.categories

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LifecycleStartEffect
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.categories.model.CategoriesEvent
import com.m.vodovoz.ui.mvi.collectAsState

@Composable
fun CategoriesEntry() = NavigationEntry<CategoriesViewModel> {
    val viewState by viewModel.collectAsState()

    LifecycleStartEffect(Unit) {
        viewModel.tabManager.changeTabVisibility(false)
        onStopOrDispose {
            viewModel.tabManager.changeTabVisibility(true)
        }
    }

    CategoriesScreen(viewModel = viewModel, viewState = viewState)

    LifecycleEffect {
        viewModel.events.collect { event ->
            when (event) {
                is CategoriesEvent.GoBackWithArguments -> {
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        "category",
                        event.currentCategory
                    )
                    navController.popBackStack()
                }

                CategoriesEvent.GoBack -> {
                    navController.popBackStack()
                }
            }
        }
    }
}
