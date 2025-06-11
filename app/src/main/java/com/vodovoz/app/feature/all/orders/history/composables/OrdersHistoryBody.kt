package com.vodovoz.app.feature.all.orders.history.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.vodovoz.app.R
import com.vodovoz.app.design_system.ExtendedTheme
import com.vodovoz.app.design_system.composables.chip.OrderStatusChip
import com.vodovoz.app.design_system.composables.chip.VodovozChip
import com.vodovoz.app.design_system.composables.chip.VodovozColorChip
import com.vodovoz.app.design_system.composables.placeholders.LoadingPlaceholder
import com.vodovoz.app.design_system.composables.tab_row.VodovozScrollableTabRow
import com.vodovoz.app.feature.all.orders.history.model.OrderFilterUi
import com.vodovoz.app.feature.all.orders.history.model.OrdersHistoryButtonUi
import com.vodovoz.app.feature.all.orders.history.model.OrdersHistoryItemUi
import com.vodovoz.app.feature.all.orders.history.model.OrdersHistoryProductUi
import com.vodovoz.app.feature.home.composables.dropShadow

@Suppress("NonSkippableComposable")
@Composable
fun OrdersHistoryBody(
    modifier: Modifier = Modifier,
    searchMode: Boolean,
    currentFilters: List<OrderFilterUi>,
    items: List<OrdersHistoryItemUi>,
    itemsLoading: Boolean,
    appendItems: Boolean,
    filters: List<OrderFilterUi>,
    onProductSee: (Int) -> Unit,
    onFilterSelect: (OrderFilterUi) -> Unit,
    onAllFiltersSelect: () -> Unit,
    onItemClick: (OrdersHistoryItemUi) -> Unit,
    onItemButtonClick: (OrdersHistoryItemUi) -> Unit,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(bottom = 8.dp)
    ) {

        if (!searchMode) {
            stickyHeader(
                key = "TabRow",
                contentType = "TabRow"
            ) {
                VodovozScrollableTabRow(
                    modifier = Modifier
                        .dropShadow(
                            shape = MaterialTheme.shapes.large,
                            color = Color.Black.copy(0.1f),
                            blur = 20.dp,
                            offsetY = 2.dp
                        )
                        .background(
                            MaterialTheme.colorScheme.background,
                            shape = MaterialTheme.shapes.large.copy(
                                topStart = CornerSize(0.dp),
                                topEnd = CornerSize(0.dp)
                            )
                        )
                        .padding(bottom = 20.dp, top = 8.dp)
                        .fillParentMaxWidth(),
                    selectedTabIndex = 0,
                    edgePadding = 16.dp,
                    spacing = 8.dp
                ) {
                    VodovozChip(
                        text = stringResource(id = R.string.all),
                        selected = currentFilters.isEmpty(),
                        onSelect = { onAllFiltersSelect() }
                    )


                    filters.forEach { filter ->
                        VodovozChip(
                            text = filter.name,
                            selected = currentFilters.contains(filter),
                            onSelect = { onFilterSelect(filter) }
                        )
                    }
                }
            }
        }

        if (itemsLoading) {
            item {
                LoadingPlaceholder(modifier = Modifier.fillParentMaxSize())
            }
        } else {
            itemsIndexed(
                items = items,
                key = { _, ordersHistoryItemUi -> ordersHistoryItemUi.id }
            ) { index, item ->

                LaunchedEffect(index) {
                    onProductSee(index)
                }

                OrdersHistoryItemCard(
                    orderHistoryItem = item,
                    onClick = onItemClick,
                    onButtonClick = onItemButtonClick
                )
            }

            if (appendItems) {
                item {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp,
                        modifier = Modifier
                            .padding(vertical = 2.dp)
                            .fillParentMaxWidth()
                            .wrapContentWidth()
                            .size(26.dp),
                        trackColor = Color.Transparent
                    )
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
            .padding(horizontal = 18.5.dp, vertical = 28.dp)
    ) {
        Row(modifier = Modifier, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(Modifier.weight(1f)) {
                Text(
                    text = AnnotatedString.fromHtml(orderHistoryItem.description),
                    color = MaterialTheme.colorScheme.surfaceTint,
                    style = MaterialTheme.typography.bodySmall
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_right),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.surfaceTint
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
                modifier = Modifier.padding(top = 8.dp),
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
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                orderHistoryItem.products.forEach { product ->
                    OrdersHistoryProductCard(
                        ordersHistoryProduct = product
                    )
                }
            }
        }
        Row(
            modifier = Modifier.padding(top = 16.dp),
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
            model = ImageRequest.Builder(LocalContext.current).data(ordersHistoryProduct.image)
                .crossfade(true).build(),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds
        )

        if (ordersHistoryProduct.quantity > 1) {
            VodovozColorChip(
                modifier = Modifier.align(Alignment.BottomEnd),
                color = MaterialTheme.colorScheme.secondary,
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
