package com.m.vodovoz.design_system.composables.card

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImagePainter
import com.m.vodovoz.R
import com.m.vodovoz.design_system.ExtendedTheme
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.composables.blur.AsyncImageBlur
import com.m.vodovoz.design_system.composables.chip.VodovozColorChipSmall
import com.m.vodovoz.design_system.model.Button
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.model.ForAdultsUi
import com.m.vodovoz.design_system.model.PricePerUnitText
import com.m.vodovoz.design_system.model.ProductUi
import com.m.vodovoz.design_system.model.notPercentLabels
import com.m.vodovoz.design_system.model.percentLabels
import com.m.vodovoz.design_system.model.widgets.LabelUi
import com.m.vodovoz.util.extensions.formatRating
import com.m.vodovoz.util.formatRoundedPrice

@Composable
fun VodovozProductCard(
    modifier: Modifier = Modifier,
    product: ProductUi,
    onClick: (ProductUi) -> Unit,
    content: @Composable () -> Unit,
) {
    VodovozOutlinedCard(
        modifier = modifier.then(
            if (product.forAdults != null) Modifier.pointerInput(Unit) {
                detectTapGestures { onClick(product) }
            }
            else Modifier
        ),
        contentPadding = PaddingValues(8.dp),
        onClick = { onClick(product) },
        content = content
    )
}

@Composable
fun GridProductCard(
    modifier: Modifier = Modifier,
    product: ProductUi,
    onClick: (ProductUi) -> Unit,
    onLike: (ProductUi) -> Unit,
    onAnalogsClick: (ProductUi) -> Unit,
    onIncrementToCart: (ProductUi) -> Unit,
    onDecrementToCart: (ProductUi) -> Unit,
) {
    VodovozProductCard(modifier = modifier, product = product, onClick = onClick) {

        val forAdults = product.forAdults

        AsyncImageBlur(
            modifier = Modifier.clip(MaterialTheme.shapes.small),
            model = product.image,
            showBlur = forAdults != null,
            placeholderText = forAdults?.textBlur ?: ""
        ) { imagePainter ->
            GridImageSection(
                imagePainter = imagePainter,
                percentLabels = product.percentLabels,
                otherLabels = product.notPercentLabels,
                isFavorite = product.isFavorite,
                onLike = { onLike(product) }
            )
        }

        Column(modifier = Modifier) {
            PriceAndRating(product = product)

            product.PricePerUnitText(modifier = Modifier.height(12.dp))

            val labelSmallVariant = ExtendedTheme.typography.labelSmallVariant.copy(
                lineHeightStyle = LineHeightStyle(
                    LineHeightStyle.Alignment.Top,
                    LineHeightStyle.Trim.None,
                ),
                fontSize = 11.sp,
            )

            Text(
                text = product.name,
                color = MaterialTheme.colorScheme.onBackground,
                style = labelSmallVariant,
                minLines = 3,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )



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

@Composable
private fun GridImageSection(
    modifier: Modifier = Modifier,
    imagePainter: AsyncImagePainter,
    percentLabels: List<LabelUi>,
    otherLabels: List<LabelUi>,
    isFavorite: Boolean,
    onLike: () -> Unit,
) {

    Box(modifier = modifier) {
        Image(
            painter = imagePainter,
            contentDescription = null,
            modifier = Modifier
                .height(105.dp)
                .fillMaxWidth(),
            contentScale = ContentScale.Inside
        )
        Row {
            percentLabels.forEach { label ->
                VodovozColorChipSmall(backgroundColor = label.backgroundColor, text = label.name)
            }
            Spacer(modifier = Modifier
                .weight(1f)
                .pointerInput(Unit) { detectTapGestures { } })

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

        FlowRow(
            modifier = Modifier.align(Alignment.BottomStart),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            otherLabels.forEach { label ->
                VodovozColorChipSmall(backgroundColor = label.backgroundColor, text = label.name)
            }
        }
    }

}

@Composable
fun PriceAndRating(modifier: Modifier = Modifier, product: ProductUi) {
    Row(
        modifier = modifier.padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.weight(1f)) {
            Text(
                modifier = Modifier.alignByBaseline(),
                text = stringResource(R.string.price, product.price.formatRoundedPrice()),
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

}


@Preview(apiLevel = 34)
@Composable
private fun GridProductCardPreview() {
    VodovozTheme {
        val sampleProduct = ProductUi(
            id = 101L,
            isFavorite = true,
            rating = 4.5f,
            price = 2900009.99f,
            oldPrice = 349000000230.99f,
            name = "Смартфон Galaxy S21Смартфон Galaxy S21Смартфон Galaxy S21Смартфон Galaxy S21Смартфон Galaxy S21Смартфон Galaxy S21Смартфон Galaxy S21Смартфон Galaxy S21Смартфон Galaxy S21",
            cartQuantity = 0,
            cartLoading = false,
            image = "https://vodovoz.net/upload/iblock/9ed/ec5cfujet9sztz077mtdzofrzjqzn0zj.jpeg",
            labels = listOf(
                LabelUi.from("Новинка", Color.Red),
                LabelUi.from("Хит продаж", Color.Green)
            ),
            isAvailable = false,
            pricePerUnit = null,
            unitOfMeasurement = null,
            forAdults = ForAdultsUi("dwqqwd", "dqwdqw", "Не нажимай", ColorfulButtonUi.Empty),
            button = null
        )

        GridProductCard(
            product = sampleProduct,
            onClick = {},
            modifier = Modifier.width(160.dp).height(255.dp),
            onLike = {},
            onAnalogsClick = {},
            onDecrementToCart = {},
            onIncrementToCart = {})
    }
}



