package com.m.vodovoz.feature.wait_feedback_products

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.core.navigation.navigateToCatalog
import com.m.vodovoz.core.navigation.navigateToProductDetails
import com.m.vodovoz.core.navigation.navigateToWriteComment
import com.m.vodovoz.feature.wait_feedback_products.model.WaitFeedbackProductsEvent
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.ui.mvi.collectEvents

@Composable
fun WaitFeedbackProductsEntry() = NavigationEntry<WaitFeedbackProductsViewModel> {
    val viewState by viewModel.collectAsState()

    WaitFeedbackProductsScreen(
        viewModel = viewModel,
        viewState = viewState
    )

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
