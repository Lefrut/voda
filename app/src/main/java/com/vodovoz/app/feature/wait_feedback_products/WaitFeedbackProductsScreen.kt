package com.vodovoz.app.feature.wait_feedback_products

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vodovoz.app.design_system.composables.placeholders.LoadingPlaceholder
import com.vodovoz.app.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.vodovoz.app.design_system.composables.placeholders.VodovozPlaceholder
import com.vodovoz.app.design_system.composables.top_bar.VodovozTopBar
import com.vodovoz.app.feature.wait_feedback_products.composables.WaitFeedbackProductsBody
import com.vodovoz.app.feature.wait_feedback_products.model.WaitFeedbackProductsState
import com.vodovoz.app.feature.wait_feedback_products.model.WaitFeedbackProductsUiState

@Composable
fun WaitFeedbackProductsScreen(
    viewModel: WaitFeedbackProductsViewModel,
    viewState: WaitFeedbackProductsState,
) {
    val uiState = viewState.uiState
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        VodovozTopBar(
            onBack = { viewModel.navigateBack() },
            title = if (uiState is WaitFeedbackProductsUiState.Empty) uiState.placeholder.title else viewState.title
        )

        when (uiState) {
            is WaitFeedbackProductsUiState.Empty -> {
                VodovozPlaceholder(
                    data = uiState.placeholder,
                    onButtonClick = {
                        viewModel.navigateToCatalog()
                    }
                )
            }

            WaitFeedbackProductsUiState.Error -> {
                NetworkErrorPlaceholder {
                    viewModel.fetchWaitFeedbackProductsDetails()
                }
            }

            WaitFeedbackProductsUiState.Loading -> {
                LoadingPlaceholder()
            }

            WaitFeedbackProductsUiState.Success -> {
                WaitFeedbackProductsBody(
                    products = viewState.items,
                    loadStates = viewState.loadStates,
                    onProductClick = { product ->
                        viewModel.navigateToProductDetails(product)
                    },
                    onProductRatingChange = { product, rating ->
                        viewModel.navigateToWriteComment(product, rating)
                    },
                    onProductSee = { index ->
                        viewModel.notifyPaging(index)
                    }
                )
            }
        }
    }
}