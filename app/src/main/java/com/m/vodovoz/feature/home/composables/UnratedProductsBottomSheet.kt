package com.m.vodovoz.feature.home.composables

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.VectorConverter
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
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.gowtham.ratingbar.RatingBar
import com.m.vodovoz.R
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.composables.bottom_sheet.VodovozDragHandle
import com.m.vodovoz.design_system.composables.image.VodovozAsyncImage
import com.m.vodovoz.design_system.modifiers.dropShadow
import com.m.vodovoz.feature.home.model.UnratedProductUi
import com.m.vodovoz.feature.home.model.UnratedProductsSectionUi
import kotlinx.coroutines.launch
import mx.platacard.pagerindicator.PagerWormIndicator

private val partiallyExpandedShape = RoundedCornerShape(
    topStart = 32.dp,
    topEnd = 32.dp,
    bottomEnd = 0.dp,
    bottomStart = 0.dp
)

private val expandedShape = RoundedCornerShape(
    topStart = 32.dp,
    topEnd = 32.dp,
    bottomEnd = 0.dp,
    bottomStart = 0.dp
)

@SuppressLint("UnusedBoxWithConstraintsScope")
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
    val scope = rememberCoroutineScope()

    val partiallyExpandedHeightPx = with(density) {
        120.dp.toPx()
    }
    val expandedPaddingTopPx = with(density) {
        32.dp.toPx()
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val layoutHeight = constraints.maxHeight.toFloat()

        val state = rememberSaveable(saver = AnchoredDraggableState.Saver()) {
            AnchoredDraggableState(initialValue = SheetValue.PartiallyExpanded)
        }

        var wasExpanded by rememberSaveable {
            mutableStateOf(false)
        }

        val expandedSheetHeightPx = with(density) {
            500.dp.toPx()
        }.coerceAtMost(
            (layoutHeight - expandedPaddingTopPx)
                .coerceAtLeast(partiallyExpandedHeightPx)
        )

        val isExpandedMode =
            wasExpanded ||
                    state.currentValue == SheetValue.Expanded ||
                    state.targetValue == SheetValue.Expanded

        val sheetHeightDp = with(density) {
            expandedSheetHeightPx.toDp()
        }

        LaunchedEffect(
            layoutHeight,
            expandedSheetHeightPx
        ) {
            if (layoutHeight <= 0f) return@LaunchedEffect

            state.updateAnchors(
                newAnchors = DraggableAnchors {
                    SheetValue.Hidden at layoutHeight
                    SheetValue.PartiallyExpanded at layoutHeight - partiallyExpandedHeightPx
                    SheetValue.Expanded at layoutHeight - expandedSheetHeightPx
                }
            )
        }

        LaunchedEffect(state.currentValue) {
            when (state.currentValue) {
                SheetValue.Expanded -> {
                    wasExpanded = true
                }

                SheetValue.Hidden -> {
                    onDispose()
                }

                SheetValue.PartiallyExpanded -> Unit
            }
        }

        LaunchedEffect(wasExpanded, state.targetValue) {
            if (wasExpanded && state.targetValue == SheetValue.PartiallyExpanded) {
                state.animateTo(SheetValue.Hidden)
            }
        }

        val hideSheet = {
            scope.launch {
                state.animateTo(SheetValue.Hidden)
            }
        }

        BackHandler(enabled = isExpandedMode) {
            hideSheet()
        }

        val animatedOffsetY = remember {
            Animatable(layoutHeight * 1.25f, Float.VectorConverter)
        }

        LaunchedEffect(Unit) {
            animatedOffsetY.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = 250,
                    delayMillis = 0,
                    easing = LinearOutSlowInEasing
                )
            )
        }

        val sheetShape = if (isExpandedMode) {
            expandedShape
        } else {
            partiallyExpandedShape
        }

        AnimatedVisibility(
            visible = isExpandedMode,
            enter = fadeIn(tween(250)),
            exit = fadeOut(tween(250))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { hideSheet() }
                    )
            )
        }

        Column(
            modifier = Modifier
                .graphicsLayer {
                    translationY = animatedOffsetY.value
                }
                .fillMaxWidth()
                .height(sheetHeightDp)
                .offset {
                    val sheetOffsetY = try {
                        state.requireOffset()
                    } catch (_: RuntimeException) {
                        layoutHeight
                    }

                    IntOffset(
                        x = 0,
                        y = sheetOffsetY.toInt()
                    )
                }
                .dropShadow(
                    shape = sheetShape,
                    color = MaterialTheme.colorScheme.onBackground.copy(
                        alpha = if (isExpandedMode) 0.2f else 0.12f
                    ),
                    blur = if (isExpandedMode) 4.dp else 20.dp,
                    offsetY = if (isExpandedMode) 0.dp else (-3).dp
                )
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = sheetShape
                )
                .clip(sheetShape)
                .anchoredDraggable(
                    state = state,
                    orientation = Orientation.Vertical
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isExpandedMode) {
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .size(width = 36.dp, height = 4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = CircleShape
                        )
                )

                Spacer(modifier = Modifier.height(16.dp))
            } else {
                VodovozDragHandle()
                Spacer(modifier = Modifier.height(8.dp))
            }

            val contentSheetValue = if (isExpandedMode) {
                SheetValue.Expanded
            } else {
                state.currentValue
            }


            AnimatedContent(
                targetState = contentSheetValue,
                label = "UpdatedProductsTransition",
                transitionSpec = {
                    when (targetState) {
                        SheetValue.Hidden -> {
                            fadeIn(snap(500)) togetherWith fadeOut(snap(500))
                        }

                        SheetValue.Expanded -> {
                            slideInVertically(
                                initialOffsetY = { height -> height }
                            ) + fadeIn(tween(250)) togetherWith
                                    slideOutVertically(
                                        targetOffsetY = { height -> -height }
                                    ) + fadeOut(tween(250))
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
                    SheetValue.Expanded -> {
                        UpdatedProductsExpanded(
                            modifier = Modifier.fillMaxWidth(),
                            title = sectionUnratedProducts.productTitle,
                            products = sectionUnratedProducts.products,
                            buttonText = sectionUnratedProducts.buttonText,
                            onProductRatingChanged = onProductRatingChanged,
                            onNoRateProductClick = onProductNoRateClick,
                        )
                    }

                    SheetValue.PartiallyExpanded -> {
                        UnratedProductsPartially(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable {
                                    scope.launch {
                                        state.animateTo(SheetValue.Expanded)
                                    }
                                },
                            title = sectionUnratedProducts.title,
                            countProductsText = sectionUnratedProducts.countProductsText,
                            products = sectionUnratedProducts.products
                        )
                    }

                    SheetValue.Hidden -> {
                        Spacer(modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}

@Suppress("NonSkippableComposable")
@Composable
fun UpdatedProductsExpanded(
    modifier: Modifier = Modifier,
    title: String,
    products: List<UnratedProductUi>,
    buttonText: String,
    onProductRatingChanged: (UnratedProductUi, Float) -> Unit,
    onNoRateProductClick: (UnratedProductUi) -> Unit,
) {
    val pagerState = rememberPagerState(0) {
        products.size
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )

        HorizontalPager(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
                .height(376.dp),
            state = pagerState,
            pageSize = PageSize.Fill,
            beyondViewportPageCount = 2,
            key = { page ->
                products.getOrNull(page)?.id ?: -page
            },
            verticalAlignment = Alignment.CenterVertically
        ) { page ->
            val product = products.getOrNull(page)

            if (product != null) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    VodovozAsyncImage(
                        modifier = Modifier.size(200.dp),
                        model = product.detailPicture,
                        contentDescription = null,
                        contentScale = ContentScale.Inside,
                    )

                    Text(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .padding(horizontal = 32.dp)
                            .wrapContentHeight(Alignment.CenterVertically),
                        text = product.name,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        textAlign = TextAlign.Center,
                        minLines = 2,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    var rating by remember(product.id) {
                        mutableFloatStateOf(0f)
                    }

                    RatingBar(
                        value = rating,
                        modifier = Modifier.padding(top = 16.dp),
                        painterEmpty = painterResource(id = R.drawable.ic_star_inactive),
                        painterFilled = painterResource(id = R.drawable.ic_star_active),
                        size = 24.dp,
                        spaceBetween = 8.dp,
                        onValueChange = { newRating ->
                            rating = newRating
                        },
                        onRatingChanged = { newRating ->
                            onProductRatingChanged(product, newRating)
                        }
                    )

                    Text(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.large)
                            .clickable {
                                onNoRateProductClick(product)
                            }
                            .padding(
                                vertical = 8.dp,
                                horizontal = 16.dp
                            ),
                        text = buttonText,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        if (pagerState.pageCount > 1) {
            PagerWormIndicator(
                modifier = Modifier.padding(top = 16.dp),
                pagerState = pagerState,
                activeDotColor = MaterialTheme.colorScheme.primary,
                dotColor = MaterialTheme.colorScheme.surfaceVariant,
                dotCount = 5,
                space = 6.dp
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
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                .padding(top = 4.dp)
                .fillMaxWidth()
                .horizontalScroll(
                    state = rememberScrollState(),
                    overscrollEffect = rememberOverscrollEffect()
                ),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            products.take(10).forEach { product ->
                key(product.id) {
                    VodovozAsyncImage(
                        modifier = Modifier.size(90.dp),
                        model = product.detailPicture,
                        contentDescription = null,
                        contentScale = ContentScale.Inside,
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
                name = "PepsiPepsiPepsiPepsiPepsiPepsiPepsiPepsiPepsiPepsiPepsiPepsiPepsiPepsiPepsiPepsiPepsiPepsiPepsiPepsiPepsiPepsiPepsiPepsiPepsiPepsiPepsiPepsiPepsiPepsiPepsiPepsiPepsiPepsiPepsiPepsi",
                id = 2L,
                detailPicture = "https://example.com/images/pepsi.png"
            ),
            UnratedProductUi(
                name = "Spriteqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq",
                id = 3L,
                detailPicture = "https://example.com/images/sprite.png"
            ),
            UnratedProductUi(
                name = "Fantadqwdddddddddddddddddddddddddddddddddddddddddddddddd",
                id = 4L,
                detailPicture = "https://example.com/images/fanta.png"
            ),
            UnratedProductUi(
                name = "",
                id = 5L,
                detailPicture = "https://example.com/images/dr_pepper.png"
            )
        )

        UpdatedProductsExpanded(
            title = "Оцените товары",
            buttonText = "Не оценивать этот товар",
            onNoRateProductClick = {},
            onProductRatingChanged = { _, _ -> },
            products = sodas
        )
    }
}