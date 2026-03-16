package com.m.vodovoz.feature.delivery_date

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LifecycleStartEffect
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.delivery_date.api.DeliveryDateNavKey
import com.m.vodovoz.feature.delivery_date.model.DeliveryDateEvent
import com.m.vodovoz.ui.mvi.collectAsState

@Composable
fun DeliveryDateEntry(navKey: DeliveryDateNavKey? = null) =
    NavigationEntry<DeliveryDateViewModel, DeliveryDateViewModel.Factory>(
        creationCallback = { factory -> factory.create(navKey) }
    ) {
    val viewState by viewModel.collectAsState()

    LifecycleStartEffect(Unit) {
        viewModel.tabManager.changeTabVisibility(false)
        onStopOrDispose {
            viewModel.tabManager.changeTabVisibility(true)
        }
    }

    DeliveryDateScreen(
        viewState = viewState,
        viewModel = viewModel
    )

    LifecycleEffect {
        viewModel.events.collect { event ->
            when (event) {
                DeliveryDateEvent.GoBack -> {
                    navigator.goBack()
                }

                is DeliveryDateEvent.GoBackToOrdering -> {
                    navigator.previousBackStackEntry?.savedStateHandle?.apply {
                        set("timeInterval", event.timeInterval)
                        set("dateOption", event.dateOption)
                        set("earlierCheckbox", event.earlierCheckbox)
                    }
                    navigator.goBack()
                }
            }
        }
    }
}
