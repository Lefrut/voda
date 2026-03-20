package com.m.vodovoz.feature.cart.composables

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.m.vodovoz.R
import com.m.vodovoz.design_system.ExtendedTheme
import com.m.vodovoz.design_system.composables.blur.VodovozBlur
import com.m.vodovoz.design_system.composables.button.QuantityButtonSmall
import com.m.vodovoz.design_system.composables.button.VodovozButton
import com.m.vodovoz.design_system.composables.button.VodovozButtonSmall
import com.m.vodovoz.design_system.composables.button.VodovozRadioButton
import com.m.vodovoz.design_system.composables.chip.VodovozColorChipSmall
import com.m.vodovoz.design_system.composables.top_bar.VodovozTopBar
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.feature.cart.model.CartPresentItemUi
import com.m.vodovoz.feature.cart.model.CartPresentUi

@Composable
fun SelectableProductsScreen(
    title: String,
    items: List<CartPresentItemUi>,
    selectedItems: List<CartPresentItemUi>,
    button: ColorfulButtonUi,
    present: CartPresentUi?,
    purchase: Boolean = false,
    showIndicator: Boolean = true,
    onBackClick: () -> Unit,
    onItemClick: (CartPresentItemUi) -> Unit,
    onImageClick: (CartPresentItemUi) -> Unit,
    onIncrementClick: (CartPresentItemUi) -> Unit = {},
    onDecrementClick: (CartPresentItemUi) -> Unit = {},
    onButtonClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        VodovozTopBar(
            onBack = onBackClick,
            title = title
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp, top = 8.dp)
        ) {
            if (present != null && present.button == null && showIndicator) {
                CartPresentIndicatorCard(
                    modifier = Modifier
                        .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
                        .animateContentSize(),
                    present = present,
                    onChoosePresentClick = {}
                )
            }

            items.forEachIndexed { index, item ->
                key(item.name + item.id) {
                    Column {
                        SelectableProductItem(
                            item = item,
                            purchase = purchase,
                            selected = selectedItems.any { selectedItem -> selectedItem.id == item.id },
                            onClick = onItemClick,
                            onImageClick = onImageClick,
                            onIncrementClick = onIncrementClick,
                            onDecrementClick = onDecrementClick
                        )

                        if (items.lastIndex != index) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 16.dp),
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }

        VodovozButton(
            modifier = Modifier.padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
            text = button.name,
            isLoading = button.loading,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = button.backgroundColor.takeOrElse { MaterialTheme.colorScheme.primary },
                contentColor = button.textColor.takeOrElse { MaterialTheme.colorScheme.background }
            ),
            onClick = onButtonClick
        )
    }
}

@Composable
private fun SelectableProductItem(
    modifier: Modifier = Modifier,
    item: CartPresentItemUi,
    purchase: Boolean,
    selected: Boolean,
    onClick: (CartPresentItemUi) -> Unit,
    onImageClick: (CartPresentItemUi) -> Unit,
    onIncrementClick: (CartPresentItemUi) -> Unit,
    onDecrementClick: (CartPresentItemUi) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (purchase) {
                    Modifier
                } else {
                    Modifier.clickable(
                        onClick = { onClick(item) },
                        interactionSource = null,
                        indication = null
                    )
                }
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val forAdults = item.forAdults
        Column(
            modifier = Modifier.width(76.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            VodovozBlur(
                modifier = Modifier.clip(MaterialTheme.shapes.extraSmall),
                showBlur = forAdults != null,
                placeholderText = "",
                placeholderImage = null
            ) {
                AsyncImage(
                    model = item.image,
                    contentDescription = null,
                    modifier = Modifier
                        .size(50.dp)
                        .clickable { onImageClick(item) },
                    contentScale = ContentScale.FillBounds
                )
            }

            val label = item.label
            if (label != null) {
                Box(
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    VodovozColorChipSmall(
                        backgroundColor = label.textColor,
                        text = label.name,
                        textColor = Color.Unspecified
                    )

                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(Modifier.padding(top = 8.dp)) {
                item.price?.let {
                    Text(
                        text = item.price,
                        modifier = Modifier.alignByBaseline(),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
                item.oldPrice?.let {
                    Text(
                        text = item.oldPrice,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .alignByBaseline(),
                        color = MaterialTheme.colorScheme.surfaceTint,
                        style = ExtendedTheme.typography.labelExtraSmallVariant.copy(
                            textDecoration = TextDecoration.LineThrough
                        )
                    )
                }
            }
        }

        if (purchase) {
            Box(modifier = Modifier.width(120.dp), contentAlignment = Alignment.Center) {
                if (item.cartQuantity > 0) {
                    QuantityButtonSmall(
                        isLoading = item.cartLoading,
                        quantity = item.cartQuantity,
                        plusEnabled = item.cartQuantity < item.maxQuantity,
                        onPlus = { onIncrementClick(item) },
                        onMinus = { onDecrementClick(item) }
                    )
                } else {
                    VodovozButtonSmall(
                        text = stringResource(id = R.string.to_cart),
                        onClick = { onIncrementClick(item) },
                        enabled = item.maxQuantity > 0,
                    )
                }
            }
        } else {
            VodovozRadioButton(selected = selected, onClick = { onClick(item) })
        }
    }
}
