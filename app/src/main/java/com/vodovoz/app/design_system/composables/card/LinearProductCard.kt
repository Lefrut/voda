package com.vodovoz.app.design_system.composables.card

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.vodovoz.app.R
import com.vodovoz.app.design_system.ExtendedTheme
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.composables.blur.VodovozBlur
import com.vodovoz.app.design_system.composables.chip.VodovozColorChipSmall
import com.vodovoz.app.design_system.model.Button
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.ForAdultsUi
import com.vodovoz.app.design_system.model.LabelUi
import com.vodovoz.app.design_system.model.PricePerUnitText
import com.vodovoz.app.design_system.model.ProductUi
import com.vodovoz.app.design_system.model.notPercentLabels
import com.vodovoz.app.design_system.model.percentLabels
import com.vodovoz.app.util.extensions.formatRating
import com.vodovoz.app.util.formatPrice
import kotlin.math.roundToInt

@Composable
fun LinearProductCard(
    modifier: Modifier = Modifier,
    product: ProductUi,
    onClick: (ProductUi) -> Unit,
    onLike: (ProductUi) -> Unit,
    onAnalogsClick: (ProductUi) -> Unit,
    onIncrementToCart: (ProductUi) -> Unit,
    onDecrementToCart: (ProductUi) -> Unit,
) {
    val percentLabels = product.percentLabels
    val otherLabels = product.notPercentLabels
    val forAdults = product.forAdults

    VodovozOutlinedCard(
        modifier = modifier,
        contentPadding = PaddingValues(8.dp),
        onClick = { onClick(product) }
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Max)) {
            VodovozBlur(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .width(144.dp)
                    .fillMaxHeight(),
                showBlur = product.forAdults != null,
                text = forAdults?.textBlur ?: ""
            ) {
                LinearImageSection(
                    image = product.image,
                    percentLabels = percentLabels,
                    otherLabels = otherLabels,
                    isFavorite = product.isFavorite,
                    onLike = { onLike(product) }
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = product.name,
                    minLines = 3,
                    maxLines = 3,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = ExtendedTheme.typography.labelSmallVariant
                )

                Row(
                    modifier = Modifier
                        .padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {

                        Text(
                            modifier = Modifier.alignByBaseline(),
                            text = stringResource(
                                R.string.price,
                                product.price.roundToInt().formatPrice()
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            maxLines = 1
                        )

                        if (product.oldPrice > product.price) {
                            Text(
                                text = stringResource(
                                    R.string.price,
                                    product.oldPrice.roundToInt().formatPrice()
                                ),
                                color = MaterialTheme.colorScheme.surfaceTint,
                                style = ExtendedTheme.typography.labelExtraSmallVariant.copy(
                                    textDecoration = TextDecoration.LineThrough
                                ),
                                modifier = Modifier
                                    .alignByBaseline()
                                    .padding(start = 8.dp)
                                    .weight(1f, false),
                                maxLines = 1,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Icon(
                        painter = painterResource(id = R.drawable.ic_star_active_v2),
                        contentDescription = null,
                        tint = if (product.rating <= 0.0f) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(18.dp),
                    )


                    Text(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(start = 2.dp),
                        text = formatRating(product.rating),
                        color = if (product.rating <= 0.0f) MaterialTheme.colorScheme.surfaceTint else MaterialTheme.colorScheme.onBackground,
                        style = ExtendedTheme.typography.labelMediumVariant,
                        maxLines = 1
                    )
                }

                product.PricePerUnitText()

                Spacer(modifier = Modifier.height(8.dp))

                product.Button(
                    isLoading = product.cartLoading,
                    onAnalogsClick = onAnalogsClick,
                    onIncrementToCart = onIncrementToCart,
                    onDecrementToCart = onDecrementToCart
                )
            }
        }
    }
}

@Suppress("NonSkippableComposable")
@Composable
private fun LinearImageSection(
    modifier: Modifier = Modifier,
    image: String,
    percentLabels: List<LabelUi>,
    otherLabels: List<LabelUi>,
    isFavorite: Boolean,
    onLike: () -> Unit,
) {
    Box(
        modifier = modifier
            .height(132.dp)
            .width(144.dp),
    ) {
        AsyncImage(
            model = image,
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Inside
        )

        Row(verticalAlignment = Alignment.Top) {
            Icon(
                painter = painterResource(id = if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_outline),
                contentDescription = null,
                tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceTint,
                modifier = Modifier
                    .size(18.dp)
                    .clickable(
                        onClick = onLike,
                        indication = null,
                        interactionSource = null
                    )
            )

            Spacer(modifier = Modifier.weight(1f))

            percentLabels.forEach { label ->
                VodovozColorChipSmall(color = label.color, text = label.name)
            }
        }

        FlowRow(
            modifier = Modifier.align(Alignment.BottomStart),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            otherLabels.forEach { label ->
                VodovozColorChipSmall(color = label.color, text = label.name)
            }
        }
    }

}

@Preview(apiLevel = 34)
@Composable
private fun LinearProductCardPreview() {
    VodovozTheme {
        val sampleProduct = ProductUi(
            id = 101L,
            isFavorite = true,
            rating = 4.5f,
            price = 2900009.99f,
            oldPrice = 349000000230.99f,
            name = "Смартфон Galaxy S21Смартфон Galaxy S21Смартфон Galaxy S21Смартфон Galaxy S21Смартфон Galaxy S21Смартфон Galaxy S21Смартфон Galaxy S21Смартфон Galaxy S21Смартфон Galaxy S21",
            cartQuantity = 1,
            cartLoading = false,
            image = "https://vodovoz.net/upload/iblock/9ed/ec5cfujet9sztz077mtdzofrzjqzn0zj.jpeg",
            labels = listOf(
                LabelUi("Новинка", Color.Red),
                LabelUi("Хит продаж", Color.Green)
            ),
            isAvailable = true,
            pricePerUnit = null,
            unitOfMeasurement = null,
            forAdults = ForAdultsUi("eqweq", "dqwdqw", "dwqdwq", ColorfulButtonUi.Empty),
            button = null,
        )


        LinearProductCard(Modifier, product = sampleProduct, onClick = {}, onLike = {}, {}, {}, {})
    }
}