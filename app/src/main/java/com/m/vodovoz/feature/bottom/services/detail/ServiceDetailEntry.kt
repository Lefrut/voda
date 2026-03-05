package com.m.vodovoz.feature.bottom.services.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.core.navigation.navigateToProductAnalogs
import com.m.vodovoz.core.navigation.navigateToProductDetails
import com.m.vodovoz.core.navigation.navigateToServiceOrder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.bottom.services.detail.model.ServiceDetailsEvent
import com.m.vodovoz.ui.mvi.collectAsState

@Composable
fun ServiceDetailEntry() = NavigationEntry<ServiceDetailsViewModel> {
    val viewState by viewModel.collectAsState()

    ServiceDetailScreen(
        viewModel = viewModel,
        viewState = viewState
    )

    LifecycleEffect {
        viewModel.events.collect { event ->
            when (event) {
                ServiceDetailsEvent.GoBack -> {
                    navController.popBackStack()
                }

                is ServiceDetailsEvent.GoToAnalogs -> {
                    navController.navigateToProductAnalogs(event.productId)
                }

                is ServiceDetailsEvent.GoToProductDetails -> {
                    navController.navigateToProductDetails(event.productId)
                }

                is ServiceDetailsEvent.GoToServiceOrder -> {
                    navController.navigateToServiceOrder(event.serviceType)
                }
            }
        }
    }
}
