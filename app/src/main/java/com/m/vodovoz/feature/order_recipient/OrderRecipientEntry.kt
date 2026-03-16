package com.m.vodovoz.feature.order_recipient

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LifecycleStartEffect
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.core.navigation.navigateToWebView
import com.m.vodovoz.feature.order_recipient.api.OrderRecipientNavKey
import com.m.vodovoz.feature.order_recipient.model.OrderRecipientEvent
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.ui.mvi.collectEvents

@Composable
fun OrderRecipientEntry(navKey: OrderRecipientNavKey? = null) =
    NavigationEntry<OrderRecipientViewModel, OrderRecipientViewModel.Factory>(
        creationCallback = { factory -> factory.create(navKey) }
    ) {
    val viewState by viewModel.collectAsState()

    LifecycleStartEffect(Unit) {
        viewModel.tabManager.changeTabVisibility(false)
        onStopOrDispose {
            viewModel.tabManager.changeTabVisibility(true)
        }
    }

    OrderRecipientScreen(
        viewModel = viewModel,
        viewState = viewState
    )

    viewModel.collectEvents { event ->
        when (event) {
            OrderRecipientEvent.GoBack -> {
                navigator.goBack()
            }

            OrderRecipientEvent.GoBackToOrdering -> {
                navigator.previousBackStackEntry?.savedStateHandle?.set(
                    "updateRecipient",
                    true
                )
                navigator.goBack()
            }

            is OrderRecipientEvent.GoToWebView -> {
                navigator.navigateToWebView(event.url, event.title)
            }
        }
    }
}
