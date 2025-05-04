package com.vodovoz.app.feature.home.wheel

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun VodovozWheel(
    range: IntRange,
    modifier: Modifier = Modifier,
    step: Int = 1,
    itemHeight: Dp = 40.dp,
    majorTickInterval: Int = 5,
    onValueChange: (Int) -> Unit = {}
) {
    // Prepare list of values
    val values = remember(range, step) { range.step(step).toList() }
    // Center index for initial scroll
    val initialIndex = values.size / 2
    val state = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

    val flingBehavior = rememberSnapFlingBehavior(lazyListState = state)
    val conf = LocalConfiguration.current

    Box(modifier) {
        LazyColumn(
            state = state,
            modifier = Modifier.fillMaxSize(),
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(vertical = (conf.screenHeightDp.dp - itemHeight) / 2)
        ) {
            itemsIndexed(values) { index, value ->
                val isSelected = state.firstVisibleItemIndex + state.layoutInfo.visibleItemsInfo
                    .indexOfFirst { it.offset == state.firstVisibleItemScrollOffset } == index
                RulerItem(
                    value = value,
                    isSelected = isSelected,
                    itemHeight = itemHeight,
                    majorTick = (value % majorTickInterval == 0)
                )
                if (isSelected) {
                    SideEffect { onValueChange(value) }
                }
            }
        }
        val primaryColor = MaterialTheme.colorScheme.primary
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .align(Alignment.Center)
                .drawBehind {
                    drawLine(
                        color = primaryColor,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 4f
                    )
                }
        )
    }
}

@Composable
private fun RulerItem(
    value: Int,
    isSelected: Boolean,
    itemHeight: Dp,
    majorTick: Boolean
) {
    val colorPrimary = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(itemHeight)
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerY = size.height / 2
            // Minor tick
            val tickLength = if (majorTick) size.width * 0.3f else size.width * 0.15f

            drawLine(
                color = if (isSelected) colorPrimary else Color.Gray,
                start = Offset(0f, centerY),
                end = Offset(tickLength, centerY),
                strokeWidth = if (majorTick) 3f else 1.5f
            )
            if (majorTick) {

                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        value.toString(),
                        tickLength + 8f,
                        centerY + 8f,
                        android.graphics.Paint().apply {
                            color = if (isSelected) colorPrimary.toArgb() else Color.Gray.toArgb()
                            textSize = 32f
                        }
                    )
                }
            }
        }
    }
}