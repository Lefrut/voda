package com.m.vodovoz.feature.order_call_you

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LifecycleStartEffect
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.feature.order_call_you.model.OrderCallYouEvent
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.ui.mvi.collectEvents

@Composable
fun OrderCallYouEntry() = NavigationEntry<OrderCallYouViewModel> {
    val viewState by viewModel.collectAsState()

    LifecycleStartEffect(Unit) {
        viewModel.tabManager.changeTabVisibility(false)
        onStopOrDispose {
            viewModel.tabManager.changeTabVisibility(true)
        }
    }

    OrderCallYouScreen(viewModel = viewModel, viewState = viewState)

    viewModel.collectEvents { event ->
        when (event) {
            OrderCallYouEvent.GoBack -> {
                navController.popBackStack()
            }

            is OrderCallYouEvent.GoBackToOrdering -> {
                navController.previousBackStackEntry?.savedStateHandle?.set(
                    "callYou",
                    event.currentItem
                )
                navController.popBackStack()
            }
        }
    }
}
