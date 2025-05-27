package com.vodovoz.app.feature.cart.composables

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.vodovoz.app.R
import com.vodovoz.app.design_system.composables.bottomLine
import com.vodovoz.app.design_system.composables.button.VodovozButton
import com.vodovoz.app.domain.general.model.cart.OrderSummaryItemUi
import com.vodovoz.app.feature.cart.model.CartButtonUi
import com.vodovoz.app.feature.cart.model.CartItemUi
import com.vodovoz.app.feature.cart.model.CartPresentUi
import com.vodovoz.app.feature.cart.model.CartPromoButtonUi
import com.vodovoz.app.feature.cart.model.ProductRestrictionUi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce

@OptIn(FlowPreview::class)
@Suppress("NonSkippableComposable")
@Composable
fun CartBody(
    modifier: Modifier = Modifier,
    countCartItemsText: String,
    cartItems: List<CartItemUi>,
    cartPresent: CartPresentUi?,
    cartOrderSummary: List<OrderSummaryItemUi>,
    bottlesButton: CartButtonUi?,
    promotionCodeButton: CartPromoButtonUi?,
    presentButton: CartButtonUi?,
    onClearCartClick: () -> Unit,
    onRemoveCartItem: (CartItemUi) -> Unit,
    onIncrementCartItem: (CartItemUi) -> Unit,
    onDecrementCartItem: (CartItemUi) -> Unit,
    onLikeCartItem: (CartItemUi) -> Unit,
    onCartItemClick: (CartItemUi) -> Unit,
    onPromotionCodeButtonClick: (CartPromoButtonUi) -> Unit,
    onPresentButtonClick: () -> Unit,
    onBottlesButtonClick: () -> Unit,
) {
    val density = LocalDensity.current


    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 28.dp),
    ) {
        item {
            cartPresent?.let {
                CartPresentCard(
                    modifier = Modifier
                        .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
                        .animateContentSize(),
                    currentCartPrice = cartOrderSummary.getOrNull(1)?.value?.filter { c ->
                        c.isDigit()
                    }?.toIntOrNull() ?: 0,
                    present = cartPresent,
                    onChoosePresentClick = onPresentButtonClick
                )
            }
        }

        item {
            Row(modifier = Modifier.padding(bottom = 24.dp, start = 16.dp, end = 16.dp)) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = countCartItemsText,
                    color = MaterialTheme.colorScheme.surfaceTint,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(id = R.string.clear_cart),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickable(onClick = onClearCartClick)
                )
            }
        }

        itemsIndexed(
            items = cartItems,
            key = { _, item -> item.id }
        ) { i, cartItem ->


            val cartItemModifier = when {
                cartItems.size == 1 -> Modifier
                i == 0 -> Modifier
                    .bottomLine(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(bottom = 16.dp)

                i == cartItems.lastIndex -> Modifier.padding(top = 16.dp)

                else -> Modifier
                    .bottomLine(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(vertical = 16.dp)

            }


            val swipeToDismissBoxState = rememberSwipeToDismissBoxState(
                positionalThreshold = with(density) {
                    { 104.dp.toPx() }
                },
                confirmValueChange = { boxValue ->
                    if (boxValue == SwipeToDismissBoxValue.EndToStart) {
                        onRemoveCartItem(cartItem)
                    }
                    true
                }
            )

            LaunchedEffect(swipeToDismissBoxState) {
                snapshotFlow { swipeToDismissBoxState.currentValue }.debounce(300).collectLatest {
                    if (swipeToDismissBoxState.currentValue != SwipeToDismissBoxValue.Settled) {
                        swipeToDismissBoxState.reset()
                    }
                }

            }


            val restriction = cartItem.restriction

            SwipeToDismissBox(
                state = swipeToDismissBoxState,
                enableDismissFromStartToEnd = false,
                enableDismissFromEndToStart = restriction != ProductRestrictionUi.FULL_RESTRICTION
                        && restriction != ProductRestrictionUi.NO_DELETE,
                backgroundContent = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.error)
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_delete),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.background
                        )
                    }
                }
            ) {
                CartItemCard(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .clickable(onClick = { onCartItemClick(cartItem) })
                        .padding(start = 16.dp, end = 16.dp)
                        .then(cartItemModifier),
                    cartItem = cartItem,
                    onClick = onCartItemClick,
                    onLikeClick = onLikeCartItem,
                    onDecrement = onDecrementCartItem,
                    onIncrement = onIncrementCartItem,
                    onRemove = onRemoveCartItem
                )
            }
        }

        item {
            bottlesButton?.apply {
                CartButton(
                    modifier = Modifier.padding(top = 24.dp, start = 16.dp, end = 16.dp),
                    image = image,
                    name = name,
                    onClick = onBottlesButtonClick
                )
            }
        }

        item {
            promotionCodeButton?.apply {
                CartButton(
                    modifier = Modifier.padding(top = 24.dp, start = 16.dp, end = 16.dp),
                    image = image,
                    name = coupon.ifEmpty { title },
                    label = text.takeIf { txt -> txt.isNotEmpty() },
                    onClick = { onPromotionCodeButtonClick(promotionCodeButton) }
                )
            }
        }

        item {
            presentButton?.apply {
                CartButton(
                    modifier = Modifier.padding(top = 24.dp, start = 16.dp, end = 16.dp),
                    image = image,
                    name = name,
                    onClick = onPresentButtonClick
                )
            }
        }

        item {
            CartOrderSummaryColumn(
                modifier = Modifier.padding(top = 24.dp, start = 16.dp, end = 16.dp),
                items = cartOrderSummary
            )
        }

        item {
            VodovozButton(
                modifier = Modifier.padding(top = 24.dp, start = 16.dp, end = 16.dp),
                text = stringResource(id = R.string.place_order),
                onClick = {
                    /*TODO*/
                }
            )
        }
    }
}