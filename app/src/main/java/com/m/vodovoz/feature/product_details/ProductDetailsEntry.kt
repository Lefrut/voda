package com.m.vodovoz.feature.product_details

import android.content.Context
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.m.vodovoz.R
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.core.navigation.NavigationEntryScope
import com.m.vodovoz.core.navigation.navigateToAboutProduct
import com.m.vodovoz.core.navigation.navigateToBrandProductList
import com.m.vodovoz.core.navigation.navigateToCategoryProductList
import com.m.vodovoz.core.navigation.navigateToDetailMedia
import com.m.vodovoz.core.navigation.navigateToPreOrder
import com.m.vodovoz.core.navigation.navigateToProductAnalogs
import com.m.vodovoz.core.navigation.navigateToProductComments
import com.m.vodovoz.core.navigation.navigateToProductDetails
import com.m.vodovoz.core.navigation.navigateToSearch
import com.m.vodovoz.core.navigation.navigateToSearchProductList
import com.m.vodovoz.core.navigation.navigateToViewedProductList
import com.m.vodovoz.core.navigation.navigateToWriteComment
import com.m.vodovoz.design_system.composables.placeholders.ForAdultsPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.product_details.api.ProductDetailsNavKey
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.util.extensions.copyText
import com.m.vodovoz.util.extensions.shareText

@Composable
fun ProductDetailsEntry(navKey: ProductDetailsNavKey? = null) =
    NavigationEntry<ProductDetailsFlowViewModel, ProductDetailsFlowViewModel.Factory>(
        creationCallback = { factory -> factory.create(navKey) }
    ) {
    val context = LocalContext.current
    val viewState by viewModel.collectAsState()
    val uiState = viewState.uiState

    val mediaPagerState = when (uiState) {
        ProductDetailsFlowViewModel.ProductDetailsUiState.Success -> {
            rememberPagerState { viewState.productDetails.mediaList.size }
        }

        else -> {
            rememberPagerState { 0 }
        }
    }

    when (uiState) {
        is ProductDetailsFlowViewModel.ProductDetailsUiState.ForAdults -> {
            ForAdultsPlaceholder(
                forAdults = uiState.forAdultsUi,
                onBackClick = {
                    viewModel.navigateBack()
                },
                onApplyClick = {
                    viewModel.setCanViewAdultProducts()
                }
            )
        }

        else -> {
            ProductDetailsScreen(
                viewState = viewState,
                viewModel = viewModel,
                mediaPagerState = mediaPagerState
            )
        }
    }

    LifecycleEffect {
        observeEvents(mediaPagerState = mediaPagerState, context = context)
    }

    LifecycleEffect {
        viewModel.listenProductDetailsUpdates(this)
    }

    LifecycleEffect {
        viewModel.listenCartUpdates()
    }
}

private suspend fun NavigationEntryScope<ProductDetailsFlowViewModel>.observeEvents(
    mediaPagerState: PagerState,
    context: Context,
): Unit =
    viewModel.events.collect { event ->
        when (event) {
            is ProductDetailsFlowViewModel.ProductDetailsEvents.GoToPreOrder -> {
                navigator.navigateToPreOrder(event.id)
            }

            is ProductDetailsFlowViewModel.ProductDetailsEvents.GoToProfile -> {
                viewModel.tabManager.setAuthRedirect(navigator.graph.id)
                viewModel.tabManager.selectTab(R.id.graph_profile)
            }

            is ProductDetailsFlowViewModel.ProductDetailsEvents.GoToCart -> {
                viewModel.tabManager.setAuthRedirect(navigator.graph.id)
                viewModel.tabManager.selectTab(R.id.graph_cart)
            }

            is ProductDetailsFlowViewModel.ProductDetailsEvents.GoToAboutProduct -> {
                navigator.navigateToAboutProduct(
                    productId = event.productId,
                    prices = event.prices,
                    analogButton = event.analogButton,
                    isAvailable = event.isAvailable
                )
            }

            is ProductDetailsFlowViewModel.ProductDetailsEvents.GoToProductComments -> {
                navigator.navigateToProductComments(
                    productId = event.productId,
                    productName = event.productName,
                    productImage = event.productImage
                )
            }

            is ProductDetailsFlowViewModel.ProductDetailsEvents.GoToProductAnalogs -> {
                navigator.navigateToProductAnalogs(event.productId)
            }

            ProductDetailsFlowViewModel.ProductDetailsEvents.GoBack -> {
                navigator.goBack()
            }

            is ProductDetailsFlowViewModel.ProductDetailsEvents.GoToSearch -> {
                navigator.navigateToSearch(event.query)
            }

            is ProductDetailsFlowViewModel.ProductDetailsEvents.GoToProductDetails -> {
                navigator.navigateToProductDetails(event.productId)
            }

            is ProductDetailsFlowViewModel.ProductDetailsEvents.GoToCategoryProductList -> {
                navigator.navigateToCategoryProductList(event.categoryId)
            }

            is ProductDetailsFlowViewModel.ProductDetailsEvents.Share -> {
                runCatching { context.shareText(event.text) }
            }

            is ProductDetailsFlowViewModel.ProductDetailsEvents.GoToSearchProductList -> {
                navigator.navigateToSearchProductList(event.query)
            }

            is ProductDetailsFlowViewModel.ProductDetailsEvents.Copy -> {
                runCatching { context.copyText(event.text) }
            }

            is ProductDetailsFlowViewModel.ProductDetailsEvents.GoToBrandProducts -> {
                navigator.navigateToBrandProductList(event.brandId)
            }

            is ProductDetailsFlowViewModel.ProductDetailsEvents.GoToWriteComment -> {
                navigator.navigateToWriteComment(
                    event.id,
                    event.name,
                    event.detailPicture,
                    event.rating
                )
            }

            is ProductDetailsFlowViewModel.ProductDetailsEvents.GoToDetailMedia -> {
                navigator.navigateToDetailMedia(event.media, event.mediaList)
            }

            is ProductDetailsFlowViewModel.ProductDetailsEvents.ScrollToMediaPage -> {
                mediaPagerState.scrollToPage(event.page)
            }

            ProductDetailsFlowViewModel.ProductDetailsEvents.GoToViewedProduct -> {
                navigator.navigateToViewedProductList()
            }
        }
    }
