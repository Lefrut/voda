package com.m.vodovoz.feature.cart.gifts

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.m.vodovoz.design_system.composables.button.VodovozButton
import com.m.vodovoz.design_system.composables.button.VodovozRadioButton
import com.m.vodovoz.design_system.composables.top_bar.VodovozTopBar
import com.m.vodovoz.feature.cart.composables.CartPresentCard
import com.m.vodovoz.feature.cart.gifts.model.GiftsState
import com.m.vodovoz.feature.cart.model.CartPresentItemUi

@Composable
fun GiftsScreen(viewModel: GiftsViewModel, viewState: GiftsState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        VodovozTopBar(
            onBack = {
                viewModel.navigateBack()
            },
            title = stringResource(id = R.string.choose_present)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp, top = 8.dp)
        ) {
            val present = viewState.present?.popupWindow?.present
            if (present != null && present.button == null) {
                CartPresentCard(
                    modifier = Modifier
                        .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
                        .animateContentSize(),
                    present = present,
                    onChoosePresentClick = {}
                )
            }

            viewState.items.forEachIndexed { index, gift ->
                key(gift.name + gift.id) {
                    Column {
                        GiftItem(
                            item = gift,
                            selected = viewState.currentGift == gift
                        ) { presentItem ->
                            viewModel.selectGift(presentItem)
                        }

                        if (viewState.items.lastIndex != index) {
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

        val button = viewState.button
        VodovozButton(
            modifier = Modifier.padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
            text = button.name,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = button.backgroundColor.takeOrElse { MaterialTheme.colorScheme.primary },
                contentColor = button.textColor.takeOrElse { MaterialTheme.colorScheme.background }
            ),
            onClick = {
                viewModel.tryToChooseGift()
            }
        )
    }
}

@Composable
private fun GiftItem(
    modifier: Modifier = Modifier,
    item: CartPresentItemUi,
    selected: Boolean,
    onClick: (CartPresentItemUi) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClick = { onClick(item) },
                interactionSource = null,
                indication = null
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val forAdults = item.forAdults
        VodovozBlur(
            modifier = Modifier.clip(MaterialTheme.shapes.extraSmall),
            showBlur = forAdults != null,
            placeholderText = "",
            placeholderImage = null
        ) {
            AsyncImage(
                model = item.image,
                contentDescription = null,
                modifier = Modifier.size(50.dp),
                contentScale = ContentScale.FillBounds
            )
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

        VodovozRadioButton(selected = selected, onClick = { onClick(item) })

    }
}