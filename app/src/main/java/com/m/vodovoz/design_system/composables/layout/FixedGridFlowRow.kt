package com.m.vodovoz.design_system.composables.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import kotlin.math.floor

@Composable
fun FixedGridFlowRow(
    modifier: Modifier = Modifier,
    itemsInRow: Int,
    horizontalSpacing: Dp,
    verticalSpacing: Dp,
    content: @Composable (itemWidth: Dp) -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val itemWidth = calcItemWidth(maxWidth, horizontalSpacing, itemsInRow)

        FlowRow(
            modifier = Modifier,
            verticalArrangement = Arrangement.spacedBy(verticalSpacing),
            horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
            maxItemsInEachRow = itemsInRow,
            maxLines = Int.MAX_VALUE
        ) {
            content(itemWidth)
        }
    }
}

private fun calcItemWidth(totalWidth: Dp, spacing: Dp, itemsInRow: Int): Dp {
    val totalSpacing = spacing * (itemsInRow - 1)
    val rawWidth = (totalWidth - totalSpacing) / itemsInRow
    return Dp(floor(rawWidth.value))
}
