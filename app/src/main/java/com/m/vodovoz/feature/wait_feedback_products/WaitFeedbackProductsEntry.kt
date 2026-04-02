package com.m.vodovoz.feature.wait_feedback_products

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.core.navigation.navigateToCatalog
import com.m.vodovoz.core.navigation.navigateToProductDetails
import com.m.vodovoz.core.navigation.navigateToWriteComment
import com.m.vodovoz.feature.wait_feedback_products.model.WaitFeedbackProductsEvent
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.ui.mvi.collectEvents

@Composable
fun WaitFeedbackProductsEntry() = NavigationEntry<WaitFeedbackProductsViewModel> {
    val lifecycleOwner = LocalLifecycleOwner.current
    val viewState by viewModel.collectAsState()

    WaitFeedbackProductsScreen(
        viewModel = viewModel,
        viewState = viewState
    )

    DisposableEffect(lifecycleOwner, navigator) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                navigator.currentBackStackEntry?.savedStateHandle?.remove<Long>("ratedProductId")
                    ?.let { productId ->
                        viewModel.removeProduct(productId)
                    }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    viewModel.collectEvents { event ->
        when (event) {
            WaitFeedbackProductsEvent.GoBack -> {
                navigator.goBack()
            }

            WaitFeedbackProductsEvent.GoToCatalog -> {
                navigator.navigateToCatalog()
            }

            is WaitFeedbackProductsEvent.GoToProductsDetails -> {
                navigator.navigateToProductDetails(event.productId)
            }

            is WaitFeedbackProductsEvent.GoToWriteComment -> {
                navigator.navigateToWriteComment(
                    event.productId,
                    event.productName,
                    event.productImage,
                    event.rating
                )
            }
        }
    }
}
