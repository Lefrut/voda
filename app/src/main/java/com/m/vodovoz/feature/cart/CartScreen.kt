package com.m.vodovoz.feature.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.m.vodovoz.R
import com.m.vodovoz.design_system.composables.button.VodovozButton
import com.m.vodovoz.design_system.composables.dialogs.VodovozDialog
import com.m.vodovoz.design_system.composables.floating.BottomFloatingContainer
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.VodovozPlaceholder
import com.m.vodovoz.design_system.composables.pull_to_refresh.VodovozPullToRefreshBox
import com.m.vodovoz.design_system.composables.scaffold.VodovozScaffold
import com.m.vodovoz.feature.cart.composables.CartBody
import com.m.vodovoz.feature.cart.composables.CartTopBar
import com.m.vodovoz.feature.cart.composables.PromotionCodeBottomSheet
import okhttp3.internal.toLongOrDefault

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(viewModel: CartFlowViewModel, viewState: CartFlowViewModel.CartState) {

    val uiState = viewState.uiState

    VodovozScaffold(
        topBar = {
            CartTopBar(
                title = viewState.title.ifEmpty {
                    uiState.placeholderOrNull?.title ?: ""
                },
                onShareClick = { }
            )
        },
        bottomBar = {
            BottomFloatingContainer {
                VodovozButton(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = stringResource(id = R.string.place_order),
                    onClick = {
                        val isZeroPrice = viewState.orderSummary.firstOrNull()?.run {
                            value.ifEmpty { displayValue }.filter { it.isDigit() }
                                .toLongOrDefault(0) <= 0
                        } ?: true

                        if (!viewState.blockOrderButton && !isZeroPrice) {
                            viewModel.navigateToOrder()
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when (uiState) {
                CartFlowViewModel.CartUiState.Cart -> {
                    VodovozPullToRefreshBox(
                        isRefreshing = viewState.showRefreshIndicator,
                        onRefresh = { viewModel.refresh() },
                    ) {
                        CartBody(
                            cartItems = viewState.items,
                            cartPresent = viewState.present,
                            countCartItemsText = viewState.countText,
                            bottlesButton = viewState.bottlesButton,
                            presentButton = viewState.presentButton,
                            promotionCodeButton = viewState.promotionalCodeButton,
                            cartOrderSummary = viewState.orderSummary,
                            onClearCartClick = {
                                viewModel.showClearCartDialog()
                            },
                            onDecrementCartItem = { cartItem ->
                                viewModel.decrementCartItem(cartItem)
                            },
                            onLikeCartItem = { cartItem ->
                                viewModel.changeFavorite(cartItem)
                            },
                            onIncrementCartItem = { cartItem ->
                                viewModel.incrementCartItem(cartItem)
                            },
                            onRemoveCartItem = { cartItem ->
                                viewModel.showTrashDialog(cartItem)
                            },
                            onCartItemClick = { cartItem ->
                                viewModel.navigateToProductDetails(cartItem)
                            },
                            onPresentButtonClick = {
                                viewModel.navigateToGifts()
                            },
                            onBottlesButtonClick = {
                                viewModel.navigateToAllBottles()
                            },
                            onPromotionCodeButtonClick = {
                                viewModel.showPromotionCodeBottomSheet()
                            }
                        )
                    }
                }

                is CartFlowViewModel.CartUiState.Empty -> {
                    VodovozPlaceholder(
                        data = uiState.placeholder,
                        onButtonClick = { viewModel.navigateToCatalog() }
                    )
                }

                CartFlowViewModel.CartUiState.Error -> {
                    NetworkErrorPlaceholder { viewModel.fetchCartDetails() }
                }

                CartFlowViewModel.CartUiState.Loading -> {
                    LoadingPlaceholder()
                }
            }
        }
    }


    if (viewState.blockCart) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(0.4f))
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitPointerEvent(PointerEventPass.Initial)
                            .changes
                            .forEach { change ->
                                change.consume()
                            }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                trackColor = Color.Transparent,
                color = MaterialTheme.colorScheme.primary,
                strokeCap = StrokeCap.Round,
                strokeWidth = 4.dp
            )
        }
    }

    if (viewState.showPromotionCodeBottomSheet && viewState.promotionalCodeButton != null) {
        PromotionCodeBottomSheet(
            info = viewState.promotionalCodeButton.popupWindow,
            promoCode = viewState.promoCode,
            onPromoCodeChange = { newValue ->
                viewModel.changePromoCode(newValue)
            },
            onDismiss = {
                viewModel.closePromoCodeBottomSheet()
            },
            onApplyPromoClick = {
                viewModel.applyPromoCode()
            }
        )
    }

    if (viewState.showClearCartDialog) {
        VodovozDialog(
            title = stringResource(id = R.string.clear_cart_title),
            description = stringResource(id = R.string.clear_cart_description),
            acceptButtonText = stringResource(id = R.string.delete).uppercase(),
            cancelButtonText = stringResource(id = R.string.clear_cart_cancel_text).uppercase(),
            onDismiss = {
                viewModel.closeClearCartDialog()
            },
            onAccept = {
                viewModel.clearCart()
            }
        )
    }

    val currentRemoveItem = viewState.currentRemoveItem
    if (viewState.showRemoveItemDialog && currentRemoveItem != null) {
        VodovozDialog(
            title = stringResource(id = R.string.delete_item_title),
            description = stringResource(id = R.string.delete_item_description),
            acceptButtonText = stringResource(id = R.string.delete_item_accept_text).uppercase(),
            cancelButtonText = stringResource(id = R.string.delete_item_cancel_text).uppercase(),
            onDismiss = {
                viewModel.closeTrashDialog()
            },
            onAccept = {
                viewModel.removeCartItem(currentRemoveItem)
            }
        )

    }
}
