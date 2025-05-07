package com.vodovoz.app.feature.bottom.services.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vodovoz.app.design_system.composables.placeholders.LoadingPlaceholder
import com.vodovoz.app.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.vodovoz.app.design_system.composables.top_bar.VodovozTopBar
import com.vodovoz.app.feature.bottom.services.detail.composables.ServiceDetailBody
import com.vodovoz.app.feature.bottom.services.detail.model.ServiceDetailState
import com.vodovoz.app.feature.bottom.services.detail.model.ServiceDetailUiState

@Composable
fun ServiceDetailScreen(
    viewModel: ServiceDetailViewModel,
    viewState: ServiceDetailState,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        VodovozTopBar(onBack = { viewModel.navigateBack() }, title = viewState.title)
        when (viewState.uiState) {
            ServiceDetailUiState.Error -> {
                NetworkErrorPlaceholder {
                    viewModel.fetchServiceDetails()
                }
            }

            ServiceDetailUiState.Loading -> {
                LoadingPlaceholder()
            }

            ServiceDetailUiState.Success -> {
                ServiceDetailBody(
                    image = viewState.image,
                    html = viewState.html,
                    productsSection = viewState.productsSection,
                    button = viewState.button,
                    onButtonClick = { button ->
                        viewModel.navigateToServiceOrder(button)
                    },
                    onProductClick = { product ->
                        viewModel.navigateToProductDetails(product)
                    },
                    onProductLike = { product ->
                        viewModel.changeFavorite(product)
                    },
                    onAnalogsClick = { product ->
                        viewModel.navigateToAnalogs(product)
                    },
                    onDecrementProductToCart = { product ->
                        viewModel.decrementProductToCard(product)
                    },
                    onIncrementProductToCart = { product ->
                        viewModel.incrementProductToCard(product)

                    }
                )
            }
        }
    }
}