package com.m.vodovoz.design_system.composables.card

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
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
import coil3.compose.AsyncImagePainter
import com.m.vodovoz.R
import com.m.vodovoz.design_system.ExtendedTheme
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.composables.blur.AsyncImageBlur
import com.m.vodovoz.design_system.composables.chip.VodovozColorChipSmall
import com.m.vodovoz.design_system.model.Button
import com.m.vodovoz.design_system.model.PricePerUnitText
import com.m.vodovoz.design_system.model.ProductUi
import com.m.vodovoz.design_system.model.notPercentLabels
import com.m.vodovoz.design_system.model.percentLabels
import com.m.vodovoz.design_system.model.widgets.LabelUi
import com.m.vodovoz.util.extensions.formatRating
import com.m.vodovoz.util.formatRoundedPrice

@Composable
fun LinearProductCard(
    modifier: Modifier = Modifier,
    product: ProductUi,
    onClick: (ProductUi) -> Unit,
    onLike: (ProductUi) -> Unit,
    showFavorite: Boolean = true,
    onAnalogsClick: (ProductUi) -> Unit,
    onIncrementToCart: (ProductUi) -> Unit,
    onDecrementToCart: (ProductUi) -> Unit,
) {
    val percentLabels = product.percentLabels
    val otherLabels = product.notPercentLabels
    val forAdults = product.forAdults

    VodovozProductCard(modifier = modifier, product = product, onClick = onClick) {
        Row(modifier = Modifier.height(IntrinsicSize.Max)) {
            AsyncImageBlur(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .width(144.dp)
                    .fillMaxHeight(),
                model = product.image,
                showBlur = product.forAdults != null,
                placeholderText = forAdults?.textBlur ?: ""
            ) { asyncImagePainter ->
                LinearImageSection(
                    imagePainter = asyncImagePainter,
                    percentLabels = percentLabels,
                    otherLabels = otherLabels,
                    isFavorite = product.isFavorite,
                    showFavorite = showFavorite,
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
                                product.price.formatRoundedPrice()
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
                                    product.oldPrice.formatRoundedPrice()
                                ),
                                color = MaterialTheme.colorScheme.surfaceTint,
                                style = ExtendedTheme.typography.labelExtraSmallVariant.copy(
                                    textDecoration = TextDecoration.LineThrough
                                ),
                                modifier = Modifier
                                    .alignByBaseline()
                                    .padding(start = 4.dp)
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
                    onAnalogsClick = onAnalogsClick,
                    onIncrementToCart = { product ->
                        if (forAdults == null) onIncrementToCart(product)
                        else onClick(product)
                    },
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
    imagePainter: AsyncImagePainter,
    percentLabels: List<LabelUi>,
    otherLabels: List<LabelUi>,
    isFavorite: Boolean,
    showFavorite: Boolean,
    onLike: () -> Unit,
) {
    Box(
        modifier = modifier
            .height(132.dp)
            .width(144.dp),
    ) {
        Image(
            painter = imagePainter,
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Inside
        )

        Row(verticalAlignment = Alignment.Top) {
            if (showFavorite) {
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
            }

            Spacer(modifier = Modifier.weight(1f))

            percentLabels.forEach { label ->
                VodovozColorChipSmall(
                    backgroundColor = label.backgroundColor,
                    text = label.name
                )
            }
        }

        FlowRow(
            modifier = Modifier.align(Alignment.BottomStart),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            otherLabels.forEach { label ->
                VodovozColorChipSmall(
                    backgroundColor = label.backgroundColor,
                    text = label.name
                )
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
                LabelUi.from("Новинка", Color.Red),
                LabelUi.from("Хит продаж", Color.Green)
            ),
            isAvailable = true,
            maxQuantity = Int.MAX_VALUE,
            pricePerUnit = null,
            unitOfMeasurement = null,
            forAdults = null,
            button = null,
        )


        LinearProductCard(
            modifier = Modifier,
            product = sampleProduct,
            onClick = {},
            onLike = {},
            onAnalogsClick = {},
            onIncrementToCart = {},
            onDecrementToCart = {}
        )
    }
}
