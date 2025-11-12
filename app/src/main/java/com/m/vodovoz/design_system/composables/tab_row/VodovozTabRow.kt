package com.m.vodovoz.design_system.composables.tab_row

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.m.vodovoz.design_system.VodovozTheme


@Composable
fun VodovozTabRow(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(4.dp),
    tabSpacing: Dp = 4.dp,
    selectedTabPosition: Int = 0,
    tabItems: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium
    ) {
        SubcomposeLayout(
            Modifier
                .padding(contentPadding)
                .selectableGroup()
        ) { constraints ->
            val preMeasured = subcompose("PreCalculate", tabItems).map { measurable ->
                measurable.measure(constraints.copy(minWidth = 0))
            }

            val tabsCount = preMeasured.size
            if (tabsCount == 0) {
                layout(constraints.maxWidth, 0) {}
            }

            val spacingPx = tabSpacing.roundToPx()
            val tabsWidth = preMeasured.map { it.width }
            val totalSpacing = if (tabsCount > 1) (tabsCount - 1) * spacingPx else 0
            val availableWidth = constraints.maxWidth - totalSpacing
            val totalTabsWidth = tabsWidth.sum()

            val useEqualWidth = totalTabsWidth <= availableWidth
            val equalWidth = availableWidth / tabsCount

            val tabWidths = if (useEqualWidth) {
                List(tabsCount) { equalWidth }
            } else {
                tabsWidth
            }

            val maxItemHeight = preMeasured.maxOf { it.height }

            val tabPositions = tabWidths.mapIndexed { index, tabWidth ->
                val x = tabWidths.take(index).sum() + spacingPx * index
                TabPosition(
                    left = x.toDp(),
                    width = tabWidth.toDp(),
                    contentWidth = Dp.Unspecified
                )
            }

            val tabPlaceables = subcompose("Tabs", tabItems).mapIndexed { index, measurable ->
                measurable.measure(
                    Constraints.fixed(
                        tabWidths[index],
                        maxItemHeight
                    )
                )
            }

            val layoutWidth = constraints.maxWidth

            layout(layoutWidth, maxItemHeight) {
                subcompose("Indicator") {
                    Box(
                        Modifier
                            .tabIndicator(
                                tabPositions.getOrNull(selectedTabPosition)
                                    ?: TabPosition(0.dp, 0.dp, Dp.Unspecified),
                                tween(durationMillis = 200, easing = LinearEasing)
                            )
                            .fillMaxWidth()
                            .height(maxItemHeight.toDp())
                            .background(
                                color = MaterialTheme.colorScheme.background,
                                shape = MaterialTheme.shapes.medium
                            )
                    )
                }.forEach { measurable ->
                    measurable.measure(Constraints.fixed(layoutWidth, maxItemHeight)).place(0, 0)
                }

                tabPlaceables.forEachIndexed { index, placeable ->
                    val pos = tabPositions[index]
                    placeable.place(x = pos.left.roundToPx(), y = 0)
                }
            }
        }
    }
}


private fun Modifier.tabIndicator(
    tabPosition: TabPosition,
    animationSpec: AnimationSpec<Dp>,
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "tabIndicatorOffset"
        value = tabPosition
    }
) {
    val currentTabWidth by animateDpAsState(
        targetValue = tabPosition.width,
        animationSpec = animationSpec, label = "currentTabWidth"
    )
    val indicatorOffset by animateDpAsState(
        targetValue = tabPosition.left,
        animationSpec = animationSpec, label = "indicatorOffset"
    )
    fillMaxWidth()
        .wrapContentSize(Alignment.BottomStart)
        .offset { IntOffset(indicatorOffset.roundToPx(), 0) }
        .width(currentTabWidth)
        .fillMaxHeight()
}

@Composable
fun VodovozTab(
    title: String,
    position: Int,
    selected: Boolean,
    textStyle: TextStyle = MaterialTheme.typography.bodySmall.copy(
        fontWeight = FontWeight.Medium
    ),
    onClick: (Int) -> Unit,
) {
    Text(
        text = title,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onClick(position) }
            .wrapContentSize(Alignment.Center)
            .padding(vertical = 6.dp),
        color = if (selected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.surfaceTint,
        style = textStyle,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}


@Preview
@Composable
private fun TabView() {

    VodovozTheme {

        var selectedTabPosition by remember { mutableIntStateOf(1) }

        val items = listOf(
            "Описание", "Апельсинки", "Водичка",
        )

        VodovozTabRow(
            modifier = Modifier.padding(horizontal = 30.dp),
            selectedTabPosition = selectedTabPosition
        ) {
            items.forEachIndexed { index, s ->
                VodovozTab(
                    title = s,
                    position = index,
                    selected = selectedTabPosition == index
                ) { selectedTabPosition = index }
            }
        }
    }
}
