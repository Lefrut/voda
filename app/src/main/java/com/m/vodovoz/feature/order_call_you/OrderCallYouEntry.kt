package com.m.vodovoz.feature.order_call_you

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.feature.cart.ordering.OrderingFlowViewModel
import com.m.vodovoz.feature.order_call_you.api.OrderCallYouNavKey
import com.m.vodovoz.feature.order_call_you.model.OrderCallYouEvent
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.ui.mvi.collectEvents

@Composable
fun OrderCallYouEntry(navKey: OrderCallYouNavKey? = null) =
    NavigationEntry<OrderCallYouViewModel, OrderCallYouViewModel.Factory>(
        creationCallback = { factory -> factory.create(navKey) }
    ) {
    val orderingViewModel = viewModel(modelClass = OrderingFlowViewModel::class)
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
                navigator.goBack()
            }

            is OrderCallYouEvent.GoBackToOrdering -> {
                orderingViewModel.setCallYou(event.currentItem)
                navigator.goBack()
            }
        }
    }
}
