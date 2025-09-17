package com.m.vodovoz.feature.promotion_details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.m.vodovoz.design_system.composables.top_bar.VodovozTopBar
import com.m.vodovoz.feature.promotion_details.composables.PromotionDetailsBody
import com.m.vodovoz.feature.promotion_details.composables.PromotionDetailsLoadingPlaceholder

@Suppress("NonSkippableComposable")
@Composable
fun PromotionDetailsScreen(
    viewModel: PromotionDetailsViewModel,
    viewState: PromotionDetailsViewModel.PromotionDetailsState,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        VodovozTopBar(onBack = { viewModel.navigateBack() }, title = "")

        when (viewState.uiState) {
            PromotionDetailsViewModel.UiState.Error -> {

            }

            PromotionDetailsViewModel.UiState.Loading -> {
                PromotionDetailsLoadingPlaceholder()
            }

            PromotionDetailsViewModel.UiState.Success -> {
                PromotionDetailsBody(
                    promotionDetails = viewState.promotionDetails,
                    products = viewState.items,
                    productsLoadStates = viewState.loadStates,
                    productsTitle = viewState.productsTitle,
                    onHyperlinkClick = { url ->
                        viewModel.navigateToWebView(url)
                    },
                    onProductClick = { product ->
                        viewModel.navigateToProductDetails(product)
                    },
                    onProductAnalogsClick = { product ->
                        viewModel.navigateToProductAnalogs(product)
                    },
                    onProductLike = { product ->
                        viewModel.changeProductFavorite(product)
                    },
                    onProductSee = { index ->
                        viewModel.notifyPaging(index)
                    },
                    onIncrementProductToCart = { product ->
                        viewModel.incrementProductToCart(product)
                    },
                    onDecrementProductToCart = { product ->
                        viewModel.decrementProductToCart(product)
                    }
                )
            }
        }
    }
}