package com.vodovoz.app.feature.home.composables

import android.graphics.BlurMaskFilter
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.crossfade
import com.gowtham.ratingbar.RatingBar
import com.vodovoz.app.R
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.composables.bottom_sheet.VodovozDragHandle
import com.vodovoz.app.feature.home.model.UnratedProductUi
import com.vodovoz.app.feature.home.model.UnratedProductsSectionUi
import mx.platacard.pagerindicator.PagerWormIndicator

private val shape = RoundedCornerShape(
    topStart = 20.dp,
    topEnd = 20.dp,
    bottomEnd = 0.dp,
    bottomStart = 0.dp
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnratedProductsBottomSheet(
    modifier: Modifier = Modifier,
    sectionUnratedProducts: UnratedProductsSectionUi,
    onDispose: () -> Unit,
    onProductRatingChanged: (UnratedProductUi, Float) -> Unit,
    onProductNoRateClick: (UnratedProductUi) -> Unit,
) {
    val density = LocalDensity.current

    val partiallyExpandedHeight = with(density) { 120.dp.toPx() }

    val expandedPaddingTopPx = with(density) { 8.dp.toPx() }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val layoutHeight = constraints.maxHeight.toFloat()

        val state = rememberSaveable(layoutHeight, saver = AnchoredDraggableState.Saver()) {
            AnchoredDraggableState(initialValue = SheetValue.PartiallyExpanded)
        }

        LaunchedEffect(layoutHeight) {
            state.updateAnchors(
                DraggableAnchors {
                    SheetValue.Hidden at (layoutHeight - expandedPaddingTopPx).coerceAtLeast(0f)
                    SheetValue.PartiallyExpanded at layoutHeight - partiallyExpandedHeight
                    SheetValue.Expanded at expandedPaddingTopPx
                }
            )

        }

        when (state.currentValue) {
            SheetValue.Hidden -> {}

            SheetValue.Expanded -> {
                BackHandler { onDispose() }
            }

            SheetValue.PartiallyExpanded -> {}
        }

        LaunchedEffect(state.currentValue, layoutHeight) {
            when (state.currentValue) {
                SheetValue.Hidden -> {
                    onDispose()
                }

                SheetValue.Expanded -> {
                    state.updateAnchors(
                        DraggableAnchors {
                            SheetValue.Hidden at (layoutHeight - expandedPaddingTopPx).coerceAtLeast(
                                0f
                            )
                            SheetValue.Expanded at expandedPaddingTopPx
                        }
                    )
                    state.animateTo(SheetValue.Expanded)
                }

                SheetValue.PartiallyExpanded -> {}
            }
        }

        val columnHeight = remember(layoutHeight) {
            with(density) { (layoutHeight - expandedPaddingTopPx).toDp() }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(columnHeight)
                .offset {
                    val sheetOffsetY = try {
                        state.requireOffset()
                    } catch (_: RuntimeException) {
                        layoutHeight / 1.2f
                    }
                    IntOffset(x = 0, y = sheetOffsetY.toInt())
                }
                .dropShadow(
                    shape = shape,
                    color = MaterialTheme.colorScheme.onBackground.copy(0.12f),
                    blur = 20.dp,
                    offsetY = (-3).dp
                )
                .background(MaterialTheme.colorScheme.background, shape)
                .clip(shape)
                .anchoredDraggable(
                    state = state,
                    orientation = Orientation.Vertical,
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            VodovozDragHandle()
            Spacer(modifier = Modifier.height(8.dp))

            AnimatedContent(
                targetState = state.currentValue,
                label = "UpdatedProductsTransition",
                transitionSpec = {
                    when (targetState) {
                        SheetValue.Hidden -> {
                            fadeIn(snap(500)) togetherWith fadeOut(snap(500))
                        }

                        SheetValue.Expanded -> {
                            slideInVertically(initialOffsetY = { height -> height }) + fadeIn(
                                tween(
                                    250
                                )
                            ) togetherWith
                                    slideOutVertically(targetOffsetY = { height -> -height }) + fadeOut(
                                tween(250)
                            )
                        }

                        SheetValue.PartiallyExpanded -> {
                            fadeIn(snap(500)) togetherWith fadeOut(snap(500))
                        }
                    }.using(
                        SizeTransform(clip = false)
                    )
                },
                contentKey = { sheetValue -> sheetValue.name }
            ) { targetState ->
                when (targetState) {
                    SheetValue.Expanded, SheetValue.Hidden -> {
                        UpdatedProductsExpanded(
                            modifier = Modifier.fillMaxSize(),
                            title = sectionUnratedProducts.productTitle,
                            products = sectionUnratedProducts.products,
                            onProductRatingChanged = onProductRatingChanged,
                            onNoRateProductClick = onProductNoRateClick,
                            onClose = onDispose
                        )
                    }

                    SheetValue.PartiallyExpanded -> {
                        UnratedProductsPartially(
                            modifier = Modifier.fillMaxSize(),
                            title = sectionUnratedProducts.title,
                            countProductsText = sectionUnratedProducts.countProductsText,
                            products = sectionUnratedProducts.products
                        )
                    }
                }
            }
        }
    }
}

fun Modifier.dropShadow(
    shape: Shape,
    color: Color = Color.Black.copy(0.25f),
    blur: Dp = 4.dp,
    offsetY: Dp = 4.dp,
    offsetX: Dp = 0.dp,
    spread: Dp = 0.dp,
) = this.drawBehind {

    val shadowSize = Size(size.width + spread.toPx(), size.height + spread.toPx())
    val shadowOutline = shape.createOutline(shadowSize, layoutDirection, this)

    val paint = Paint()
    paint.color = color

    if (blur.toPx() > 0) {
        paint.asFrameworkPaint().apply {
            maskFilter = BlurMaskFilter(blur.toPx(), BlurMaskFilter.Blur.NORMAL)
        }
    }

    drawIntoCanvas { canvas ->
        canvas.save()
        canvas.translate(offsetX.toPx(), offsetY.toPx())
        canvas.drawOutline(shadowOutline, paint)
        canvas.restore()
    }
}

@Suppress("NonSkippableComposable")
@Composable
fun UpdatedProductsExpanded(
    modifier: Modifier = Modifier,
    title: String,
    products: List<UnratedProductUi>,
    onProductRatingChanged: (UnratedProductUi, Float) -> Unit,
    onNoRateProductClick: (UnratedProductUi) -> Unit,
    onClose: () -> Unit,
) {
    val pagerState = rememberPagerState(products.size) { products.size }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.size(24.dp))

            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_close),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .clickable { onClose() }
            )
        }

        HorizontalPager(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
                .weight(1f),
            state = pagerState,
            pageSize = PageSize.Fill,
            beyondViewportPageCount = 2,
            key = { page ->
                products.getOrNull(page)?.id ?: -page
            },
            verticalAlignment = Alignment.CenterVertically
        ) lambda@{ page ->
            val product = products.getOrNull(page)
            if (product != null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        modifier = Modifier.size(300.dp),
                        model = product.detailPicture,
                        contentDescription = null,
                        contentScale = ContentScale.Inside,
                    )
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = product.name,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        minLines = 3,
                        maxLines = 3
                    )


                    var rating by rememberSaveable {
                        mutableFloatStateOf(0f)
                    }

                    RatingBar(
                        value = rating,
                        modifier = Modifier.padding(top = 32.dp),
                        painterEmpty = painterResource(id = R.drawable.ic_star_inactive),
                        painterFilled = painterResource(id = R.drawable.ic_star_active),
                        size = 48.dp,
                        spaceBetween = 8.dp,
                        onValueChange = { newRating ->
                            rating = newRating
                        },
                        onRatingChanged = { newRating ->
                            onProductRatingChanged(product, newRating)
                        }
                    )
                }
            }
        }

        if (pagerState.pageCount > 1) {
            PagerWormIndicator(
                modifier = Modifier.padding(vertical = 12.dp),
                pagerState = pagerState,
                activeDotColor = MaterialTheme.colorScheme.primary,
                dotColor = MaterialTheme.colorScheme.surfaceVariant,
                dotCount = 5,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp), contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.do_not_rate_this_product),
                color = MaterialTheme.colorScheme.surfaceTint,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.large)
                    .clickable {
                        onNoRateProductClick(products[pagerState.currentPage])
                    }
                    .padding(12.dp)
            )
        }
    }
}

@Suppress("NonSkippableComposable")
@Composable
fun UnratedProductsPartially(
    modifier: Modifier = Modifier,
    title: String,
    countProductsText: String,
    products: List<UnratedProductUi>,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleSmall
        )
        Text(
            text = countProductsText,
            color = MaterialTheme.colorScheme.surfaceTint,
            style = MaterialTheme.typography.labelMedium
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(
                    rememberScrollState(),
                    rememberOverscrollEffect()
                ),
            horizontalArrangement = Arrangement.Center
        ) {
            products.take(10).forEach { product ->
                key(product.id) {
                    AsyncImage(
                        modifier = Modifier
                            .size(120.dp),
                        model = product.detailPicture,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        }
    }
}

@Preview(apiLevel = 34)
@Composable
private fun UnratedProductsExpandedPreview() {
    VodovozTheme {

        val sodas = listOf(
            UnratedProductUi(
                name = "Coca-Cola",
                id = 1L,
                detailPicture = "https://example.com/images/pepsi.png"
            ),
            UnratedProductUi(
                name = "Pepsi",
                id = 2L,
                detailPicture = "https://example.com/images/pepsi.png"
            ),
            UnratedProductUi(
                name = "Sprite",
                id = 3L,
                detailPicture = "https://example.com/images/sprite.png"
            ),
            UnratedProductUi(
                name = "Fanta",
                id = 4L,
                detailPicture = "https://example.com/images/fanta.png"
            ),
            UnratedProductUi(
                name = "Dr Pepper",
                id = 5L,
                detailPicture = "https://example.com/images/dr_pepper.png"
            )
        )


        UpdatedProductsExpanded(
            title = "Это нижний лист",
            onClose = {

            },
            onNoRateProductClick = {

            },
            onProductRatingChanged = { _, _ ->

            },
            products = sodas
        )
    }
}