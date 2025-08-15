package com.vodovoz.app.feature.product_details

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import com.vodovoz.app.design_system.composables.button.ProductBottomFloatingButton
import com.vodovoz.app.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.vodovoz.app.feature.product_details.composables.MultiProductBottomSheet
import com.vodovoz.app.feature.product_details.composables.PresentBottomSheet
import com.vodovoz.app.feature.product_details.composables.ProductDetailsBody
import com.vodovoz.app.feature.product_details.composables.ProductDetailsPlaceholder
import com.vodovoz.app.feature.product_details.composables.ProductDetailsTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsScreen(
    viewState: ProductDetailsFlowViewModel.ProductDetailsState,
    viewModel: ProductDetailsFlowViewModel,
    mediaPagerState: PagerState,
) {
    val productDetails = viewState.productDetails

    val floatingButtonProgress by animateFloatAsState(
        targetValue = if (viewState.hideFloatingButton) 1f else 0f,
        animationSpec = tween(easing = LinearEasing, durationMillis = 100),
        label = "floatingButtonProgress"
    )

    Scaffold(
        topBar = {
            ProductDetailsTopBar(
                onNavigationClick = {
                    viewModel.navigateBack()
                },
                onLikeClick = {
                    viewModel.changeFavorite()
                },
                onShareClick = {
                    viewModel.share()
                },
                isFavoriteProduct = productDetails.isFavorite,
                loading = viewState.uiState is ProductDetailsFlowViewModel.ProductDetailsUiState.Loading,
                showActionIcons = viewState.uiState is ProductDetailsFlowViewModel.ProductDetailsUiState.Success
            )
        },
        bottomBar = {
            ProductBottomFloatingButton(
                modifier = Modifier.graphicsLayer {
                    viewState.hideFloatingButton
                    translationY = lerp(0f, size.height, floatingButtonProgress)
                },
                isLoading = viewState.buttonIsLoading,
                cartQuantity = productDetails.cartQuantity,
                totalPrice = viewState.totalPrice,
                oldPrice = productDetails.firstPrice.oldPrice.toInt(),
                price = productDetails.firstPrice.price.toInt(),
                giftText = viewState.presentInfo.html,
                isAvailable = productDetails.isAvailable,
                analogButton = viewState.buttons.analogButton,
                onIncrementProduct = {
                    viewModel.incrementCart()
                },
                onDecrementProduct = {
                    viewModel.decrementCart()
                },
                onAnalogClick = {
                    viewModel.navigateToProductAnalogs()
                }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        when (viewState.uiState) {
            ProductDetailsFlowViewModel.ProductDetailsUiState.Loading -> {
                ProductDetailsPlaceholder(
                    modifier = Modifier.padding(top = paddingValues.calculateTopPadding())
                )
            }

            ProductDetailsFlowViewModel.ProductDetailsUiState.Error -> {
                NetworkErrorPlaceholder { viewModel.fetchProductDetails() }
            }

            is ProductDetailsFlowViewModel.ProductDetailsUiState.ForAdults -> {}

            ProductDetailsFlowViewModel.ProductDetailsUiState.Success -> {
                ProductDetailsBody(
                    modifier = Modifier
                        .padding(top = paddingValues.calculateTopPadding())
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = paddingValues.calculateBottomPadding()),
                    mediaPagerState = mediaPagerState,
                    productDetails = productDetails,
                    comments = viewState.comments,
                    quantityButtonIsLoading = viewState.buttonIsLoading,
                    productCartQuantity = productDetails.cartQuantity,
                    showAllProperties = viewState.showAllProperties,
                    moreProductSections = viewState.items,
                    showDetailText = viewState.showDetailText,
                    buttons = viewState.buttons,
                    totalPrice = viewState.totalPrice,
                    onFloatingButtonChange = { isVisible ->
                        viewModel.changeFloatingButton(isVisible)
                    },
                    onAllPropertiesShowOrHide = {
                        viewModel.showAllOrHideProperties()
                    },
                    onDescriptionShowOrHide = {
                        viewModel.showOrHideDetailText()
                    },
                    onProductMediaClick = { media ->
                        viewModel.navigateToDetailMedia(media)
                    },
                    onIncrementProduct = {
                        viewModel.incrementCart()
                    },
                    onDecrementProduct = {
                        viewModel.decrementCart()
                    },
                    onAnalogButtonClick = {
                        viewModel.navigateToProductAnalogs()
                    },
                    onPreOrderButtonClick = {
                        viewModel.navigateToPreOrder()
                    },
                    onPresentButtonClick = {
                        viewModel.showPresentBottomSheet()
                    },
                    onMultiButtonClick = {
                        viewModel.showMultiBottomSheet()
                    },
                    onPresentBlockButtonClick = {
                        viewModel.showPresentBlockBottomSheet()
                    },
                    onAboutProductClick = {
                        viewModel.navigateToAboutProduct()
                    },
                    onShowAllCommentsClick = {
                        viewModel.showAllComments()
                    },
                    onQueryClick = { query ->
                        viewModel.navigateToSearch(query)
                    },
                    onProductClick = { product ->
                        viewModel.navigateToProductDetails(product)
                    },
                    onProductLikeClick = { product ->
                        viewModel.changeFavorite(product)
                    },

                    onBrandClick = { brandItem ->
                        viewModel.navigateToBrandProducts(brandItem)
                    },
                    onCategoryClick = { categoryItem ->
                        viewModel.navigateToCategory(categoryItem)
                    },
                    onCopyArticleNumberClick = {
                        viewModel.copyArticleNumber()
                    },
                    onIncrementProductToCart = { product ->
                        viewModel.incrementProductToCart(product)
                    },
                    onDecrementProductToCart = { product ->
                        viewModel.decrementProductToCart(product)
                    },
                    onProductAnalogsClick = { product ->
                        viewModel.navigateToProductAnalogs(product)
                    },
                    onWriteCommentClick = {
                        viewModel.navigateToWriteComment()
                    }
                )

            }
        }
    }

    if (viewState.showMultiBottomSheet) {
        MultiProductBottomSheet(
            cartQuantity = viewState.multiProductQuantity,
            totalPrice = viewState.multiProductTotalPrice,
            firstPrice = productDetails.firstPrice,
            prices = productDetails.prices,
            onDismissRequest = {
                viewModel.hideMultiBottomSheet()
            },
            onMultiProductQuantity = { newMultiProductQuantity ->
                viewModel.changeMultiProductQuantity(newMultiProductQuantity)
            },
            onPlus = {
                viewModel.incrementMultiProduct()
            },
            onMinus = {
                viewModel.decrementMultiProduct()
            },
            onSaveClick = {
                viewModel.saveMultiProductChoice()
            }
        )
    }

    val presentButton = viewState.buttons.blockButton
    if (viewState.showPresentBottomSheet && presentButton != null) {
        PresentBottomSheet(
            data = presentButton.data,
            button = presentButton.buyButton,
            onDismissRequest = { viewModel.hidePresentBottomSheet() },
            onBuyButtonClick = {
                viewModel.addProductWithGift(it)
            }
        )
    }

    val presentBlock = viewState.buttons.blockDesignButton
    if (viewState.showPresentBlockBottomSheet && presentBlock != null) {
        PresentBottomSheet(
            data = presentBlock.data,
            button = presentBlock.buyButton,
            onDismissRequest = { viewModel.hidePresentBlockBottomSheet() },
            onBuyButtonClick = {
                viewModel.addProductWithGift(it)
            }
        )
    }
}