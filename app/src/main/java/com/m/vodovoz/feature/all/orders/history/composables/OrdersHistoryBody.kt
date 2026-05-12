package com.m.vodovoz.feature.all.orders.history.composables

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import com.m.vodovoz.R
import com.m.vodovoz.design_system.ExtendedTheme
import com.m.vodovoz.design_system.composables.card.GridProductCard
import com.m.vodovoz.design_system.composables.chip.OrderStatusChip
import com.m.vodovoz.design_system.composables.chip.VodovozChip
import com.m.vodovoz.design_system.composables.chip.VodovozColorChip
import com.m.vodovoz.design_system.composables.decoration.SmallBannerPager
import com.m.vodovoz.design_system.composables.list.GridEdgePadding
import com.m.vodovoz.design_system.composables.list.GridHorizontalPadding
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.VodovozPlaceholder
import com.m.vodovoz.design_system.composables.tab_row.VodovozTab
import com.m.vodovoz.design_system.composables.tab_row.VodovozTabRow
import com.m.vodovoz.design_system.composables.tab_row.VodovozScrollableTabRow
import com.m.vodovoz.design_system.model.AboutAdvertisingUi
import com.m.vodovoz.design_system.model.BannerUi
import com.m.vodovoz.design_system.model.ProductUi
import com.m.vodovoz.design_system.model.VodovozPlaceholderUi
import com.m.vodovoz.feature.all.orders.history.model.OrdersHistoryButtonUi
import com.m.vodovoz.feature.all.orders.history.model.OrdersHistoryItemUi
import com.m.vodovoz.feature.all.orders.history.model.OrdersHistoryProductUi
import com.m.vodovoz.feature.all.orders.history.model.OrdersHistoryTabUi
import com.m.vodovoz.feature.home.composables.TitleAndButton

private fun measuredHeightOrZero(
    shouldCount: Boolean,
    heightPx: Int
): Int {
    return if (shouldCount) {
        heightPx
    } else {
        0
    }
}

@Composable
fun OrdersHistoryBody(
    modifier: Modifier = Modifier,
    searchMode: Boolean,
    tabs: List<OrdersHistoryTabUi>,
    selectedTabIndex: Int,
    currentYear: String?,
    currentTabPlaceholder: VodovozPlaceholderUi?,
    itemsLoading: Boolean,
    appendItems: Boolean,
    banners: List<BannerUi>,
    onProductSee: (Int) -> Unit,
    onTabSelect: (Int) -> Unit,
    onYearSelect: (String) -> Unit,
    onItemClick: (OrdersHistoryItemUi) -> Unit,
    onItemButtonClick: (OrdersHistoryItemUi) -> Unit,
    onBannerClick: (BannerUi) -> Unit,
    onAboutAdvertisingClick: (AboutAdvertisingUi) -> Unit,
    onPlaceholderButtonClick: () -> Unit,
    products: List<ProductUi>,
    productsTitle: String,
    orders: List<OrdersHistoryItemUi>,
    onOrderSee: (Int) -> Unit,
    onProductClick: (ProductUi) -> Unit,
    onProductLike: (ProductUi) -> Unit,
    onProductAnalogsClick: (ProductUi) -> Unit,
    onIncrementProductToCart: (ProductUi) -> Unit,
    onDecrementProductToCart: (ProductUi) -> Unit,
) {
    val density = LocalDensity.current

    val years = tabs.getOrNull(selectedTabIndex)?.years.orEmpty()
    val selectedYearIndex = years.indexOf(currentYear)

    var columnHeightPx by remember { mutableIntStateOf(0) }
    var bannersHeightPx by remember { mutableIntStateOf(0) }
    var topSpacerHeightPx by remember { mutableIntStateOf(0) }
    var tabsYearsHeightPx by remember { mutableIntStateOf(0) }
    var bottomSpacerHeightPx by remember { mutableIntStateOf(0) }

    val bottomListPadding = 8.dp
    val bottomListPaddingPx = with(density) {
        bottomListPadding.roundToPx()
    }

    val hasTopSpacer = tabs.isNotEmpty() || years.isNotEmpty()

    val placeholderHeightDp = with(density) {
        val remainingHeightPx =
            columnHeightPx -
                    measuredHeightOrZero(
                        shouldCount = banners.isNotEmpty(),
                        heightPx = bannersHeightPx
                    ) -
                    measuredHeightOrZero(
                        shouldCount = hasTopSpacer,
                        heightPx = topSpacerHeightPx
                    ) -
                    measuredHeightOrZero(
                        shouldCount = !searchMode,
                        heightPx = tabsYearsHeightPx
                    ) -
                    measuredHeightOrZero(
                        shouldCount = !searchMode,
                        heightPx = bottomSpacerHeightPx
                    ) -
                    bottomListPaddingPx

        remainingHeightPx.coerceAtLeast(0).toFloat().toDp()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .onSizeChanged {
                columnHeightPx = it.height
            }
    ) {
        if (banners.isNotEmpty()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .onSizeChanged {
                        bannersHeightPx = it.height
                    }
                    .background(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.shapes.large
                    )
            ) {
                SmallBannerPager(
                    banners = banners,
                    onBannerClick = onBannerClick,
                    onAboutAdvertisingClick = onAboutAdvertisingClick
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = bottomListPadding)
        ) {
            stickyHeader {
                if (hasTopSpacer) {
                    Spacer(
                        Modifier
                            .height(8.dp)
                            .fillMaxWidth()
                            .onSizeChanged {
                                topSpacerHeightPx = it.height
                            }
                            .background(MaterialTheme.colorScheme.background)
                    )
                }
            }

            if (!searchMode) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .onSizeChanged {
                                tabsYearsHeightPx = it.height
                            }
                    ) {
                        if (tabs.isNotEmpty()) {
                            VodovozTabRow(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.background)
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp),
                                selectedTabPosition = selectedTabIndex
                            ) {
                                tabs.forEachIndexed { index, tab ->
                                    VodovozTab(
                                        title = tab.name,
                                        position = index,
                                        selected = selectedTabIndex == index,
                                        onClick = onTabSelect
                                    )
                                }
                            }
                        }

                        if (years.isNotEmpty()) {
                            VodovozScrollableTabRow(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(top = 8.dp)
                                    .fillMaxWidth(),
                                selectedTabIndex = selectedYearIndex,
                                edgePadding = 16.dp,
                                spacing = 8.dp,
                            ) {
                                years.forEach { year ->
                                    VodovozChip(
                                        text = year,
                                        selected = currentYear == year,
                                        onSelect = { onYearSelect(year) }
                                    )
                                }
                            }
                        }
                    }
                }

                stickyHeader {
                    Spacer(
                        Modifier
                            .height(12.dp)
                            .fillMaxWidth()
                            .onSizeChanged {
                                bottomSpacerHeightPx = it.height
                            }
                            .background(
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.shapes.large.copy(
                                    topStart = CornerSize(0.dp),
                                    topEnd = CornerSize(0.dp)
                                )
                            )
                    )
                }
            }

            if (itemsLoading) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LoadingPlaceholder(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .height(placeholderHeightDp),
                        containerColor = Color.Transparent
                    )
                }
            } else if (orders.isEmpty() && currentTabPlaceholder != null) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    VodovozPlaceholder(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth()
                            .height(placeholderHeightDp),
                        data = currentTabPlaceholder,
                        onButtonClick = onPlaceholderButtonClick
                    )
                }
            } else {
                itemsIndexed(
                    items = orders,
                    span = { i, item -> GridItemSpan(maxLineSpan) }
                ) { index, item ->
                    LaunchedEffect(index) {
                        onOrderSee(index)
                    }

                    OrdersHistoryItemCard(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .animateItem(fadeOutSpec = null),
                        orderHistoryItem = item,
                        onClick = onItemClick,
                        onButtonClick = onItemButtonClick
                    )
                }

                if(productsTitle.isNotEmpty()){
                    item {
                        TitleAndButton(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .background(MaterialTheme.colorScheme.background, shape = MaterialTheme.shapes.large)
                                .padding(bottom = 8.dp),
                            title = productsTitle,
                            button = null
                        )
                    }
                }

                itemsIndexed(products) { i, product ->
                    LaunchedEffect(i) {
                        onProductSee(i)
                    }

                    if (i != products.lastIndex || products.size % 2 == 0) {
                        GridHorizontalPadding(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.background)
                                .padding(top = 8.dp),
                            isStartPadding = i % 2 == 0
                        ) {
                            GridProductCard(
                                product = product,
                                onAnalogsClick = onProductAnalogsClick,
                                onLike = onProductLike,
                                onClick = onProductClick,
                                onDecrementToCart = onDecrementProductToCart,
                                onIncrementToCart = onIncrementProductToCart
                            )
                        }

                    }
                }

                if (appendItems) {
                    item(span = { GridItemSpan(2) }) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp,
                            modifier = Modifier
                                .padding(vertical = 2.dp)
                                .fillMaxWidth()
                                .wrapContentWidth()
                                .size(26.dp),
                            trackColor = Color.Transparent
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrdersHistoryItemCard(
    modifier: Modifier = Modifier,
    orderHistoryItem: OrdersHistoryItemUi,
    onClick: (OrdersHistoryItemUi) -> Unit,
    onButtonClick: (OrdersHistoryItemUi) -> Unit,
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(
                MaterialTheme.colorScheme.background,
                MaterialTheme.shapes.large
            )
            .clickable { onClick(orderHistoryItem) }
            .padding(vertical = 28.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.5.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.weight(1f)) {

                FirstLineWithIcon(
                    text = orderHistoryItem.description,
                    iconId = R.drawable.ic_arrow_right
                )
            }
            orderHistoryItem.status?.let {
                OrderStatusChip(
                    modifier = Modifier.padding(start = 12.dp),
                    status = orderHistoryItem.status
                )
            }
        }

        if (orderHistoryItem.address.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(top = 8.dp, start = 18.5.dp, end = 18.5.dp),
                text = orderHistoryItem.address,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.sp)
            )
        }

        if (orderHistoryItem.products.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(start = 18.5.dp, end = 18.5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                orderHistoryItem.products.forEach { product ->
                    key(product.id) {
                        OrdersHistoryProductCard(
                            ordersHistoryProduct = product
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier.padding(top = 16.dp, start = 18.5.dp, end = 18.5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = orderHistoryItem.priceText,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall
            )

            orderHistoryItem.button?.let { button ->
                OrdersHistoryButton(
                    button = button,
                    onClick = {
                        onButtonClick(orderHistoryItem)
                    }
                )
            }
        }
    }
}

@Composable
fun OrdersHistoryProductCard(
    modifier: Modifier = Modifier,
    ordersHistoryProduct: OrdersHistoryProductUi,
) {
    Box(modifier = modifier.size(76.dp)) {
        AsyncImage(
            model = ordersHistoryProduct.image,
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds
        )

        if (ordersHistoryProduct.quantity > 1) {
            VodovozColorChip(
                modifier = Modifier.align(Alignment.BottomEnd),
                backgroundColor = MaterialTheme.colorScheme.secondary,
                text = stringResource(id = R.string.quanitity_x, ordersHistoryProduct.quantity)
            )
        }
    }
}

@Composable
private fun OrdersHistoryButton(
    modifier: Modifier = Modifier,
    button: OrdersHistoryButtonUi,
    onClick: (OrdersHistoryButtonUi) -> Unit,
) {
    FilledTonalButton(
        modifier = modifier
            .widthIn(144.dp)
            .height(38.dp),
        onClick = { onClick(button) },
        contentPadding = PaddingValues(horizontal = 8.dp),
        shape = MaterialTheme.shapes.large,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = button.background,
            contentColor = button.color
        )
    ) {
        if (button.image.isNotBlank()) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = button.image,
                    contentScale = ContentScale.FillBounds
                ),
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(18.dp),
                contentScale = ContentScale.FillBounds,
                colorFilter = ColorFilter.tint(button.color)
            )
        }
        Text(
            text = button.name,
            color = button.color,
            style = ExtendedTheme.typography.buttonSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


@Composable
private fun FirstLineWithIcon(
    text: String,
    @DrawableRes
    iconId: Int,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.surfaceTint,
    textStyle: TextStyle = MaterialTheme.typography.bodySmall,
) {
    var firstLine by remember { mutableStateOf(text) }
    var restLines by remember { mutableStateOf("") }

    Box(modifier = modifier) {
        Row {
            Text(
                modifier = Modifier.weight(1f, false),
                text = text,
                style = textStyle,
                maxLines = Int.MAX_VALUE,
                onTextLayout = { layoutResult ->
                    if (layoutResult.lineCount > 1) {
                        val firstLineEnd = layoutResult.getLineEnd(0, visibleEnd = true)
                        firstLine = text.substring(0, firstLineEnd)
                        restLines = text.substring(firstLineEnd)
                    }
                },
                color = Color.Transparent
            )
            Icon(
                painter = painterResource(id = iconId),
                contentDescription = null,
                tint = Color.Transparent,
                modifier = Modifier.size(24.dp)
            )
        }
        Column(Modifier.matchParentSize()) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    modifier = Modifier.weight(1f, false),
                    text = firstLine,
                    style = textStyle,
                    color = contentColor
                )
                Icon(
                    painter = painterResource(id = iconId),
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            if (restLines.isNotEmpty()) {
                Text(
                    text = restLines.trim(),
                    style = textStyle,
                    color = contentColor
                )
            }
        }
    }

}
