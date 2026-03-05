package com.m.vodovoz.feature.payment_method

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.feature.payment_method.model.PaymentMethodEvent
import com.m.vodovoz.feature.payment_method.model.toNav
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.ui.mvi.collectEvents

@Composable
fun PaymentMethodEntry() = NavigationEntry<PaymentMethodViewModel> {
    val lifecycleOwner = LocalLifecycleOwner.current
    val viewState by viewModel.collectAsState()

    PaymentMethodScreen(viewModel = viewModel, viewState = viewState)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    viewModel.tabManager.changeTabVisibility(false)
                }

                Lifecycle.Event.ON_PAUSE -> {
                    viewModel.tabManager.changeTabVisibility(true)
                }

                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    viewModel.collectEvents { event ->
        when (event) {
            PaymentMethodEvent.GoBack -> {
                navController.popBackStack()
            }

            is PaymentMethodEvent.GoBackToOrdering -> {
                navController.previousBackStackEntry?.savedStateHandle?.apply {
                    set("paymentBalance", event.paymentBalance?.toNav())
                    set("paymentBonuses", event.paymentBonuses?.toNav())
                    set("paymentMethod", event.paymentMethod?.toNav())
                }
                navController.popBackStack()
            }
        }
    }
}
