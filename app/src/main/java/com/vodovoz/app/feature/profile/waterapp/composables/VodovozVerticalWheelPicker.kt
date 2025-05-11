package com.vodovoz.app.feature.profile.waterapp.composables

import androidx.compose.ui.util.lerp

import android.graphics.Typeface
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import com.vodovoz.app.R
import com.vodovoz.app.design_system.VodovozTheme
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.abs

@Immutable
data class VerticalLineUi(
    val index: Int,
    val offsetY: Float,
    val height: Float
)

@Composable
fun <T : Any> VodovozWheelPickerVertical(
    modifier: Modifier = Modifier,
    items: List<T>,
    initialIndex: Int = 0,
    markedNumber: Int = 5,
    itemText: (T) -> String = { it.toString() },
    onMiddleItemChange: (T) -> Unit,
) {
    Box(
        modifier = modifier
            .height(318.dp)
            .width(158.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Image(
            modifier = Modifier
                .padding(end = 17.dp)
                .fillMaxHeight()
                .width(82.dp),
            painter = painterResource(id = R.drawable.bg_vertical_wheel),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.surfaceVariant.copy(0.5f))
        )

        VodovozVerticalWheelCore(
            modifier = Modifier.padding(end = 38.dp),
            items = items,
            itemText = itemText,
            markedNumber = markedNumber,
            initialIndex = initialIndex,
            onMiddleItemChange = onMiddleItemChange
        )

        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}

@OptIn(FlowPreview::class)
@Composable
internal fun <T : Any> VodovozVerticalWheelCore(
    modifier: Modifier = Modifier,
    items: List<T>,
    initialIndex: Int = 0,
    markedNumber: Int = 5,
    itemText: (T) -> String,
    onMiddleItemChange: (T) -> Unit,
) {

    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val style: TextStyle = MaterialTheme.typography.headlineSmall
    val resolver = LocalFontFamilyResolver.current
    val typeface = remember(resolver, style) {
        resolver.resolve(
            fontFamily = style.fontFamily,
            fontWeight = style.fontWeight ?: FontWeight.Normal,
            fontStyle = style.fontStyle ?: FontStyle.Normal,
            fontSynthesis = style.fontSynthesis ?: FontSynthesis.All,
        ).value as Typeface
    }
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceTint = MaterialTheme.colorScheme.surfaceTint
    val onBg = MaterialTheme.colorScheme.onBackground

    var viewportHeightPx by rememberSaveable { mutableFloatStateOf(0f) }
    val verticalPaddingPx = viewportHeightPx / 1.2f

    val lineDataList = remember(items, viewportHeightPx) {
        if (viewportHeightPx == 0f) return@remember emptyList()

        val spacePx = with(density) { VodovozWheelPickerDefaults.space.toPx() }
        val hPx = with(density) { VodovozWheelPickerDefaults.lineWidth.toPx() }
        val mhPx = with(density) { VodovozWheelPickerDefaults.markedLineWidth.toPx() }
        List(items.size) { idx ->
            val marksBefore = idx / markedNumber
            val unmarks = idx - marksBefore
            val offsetY = marksBefore * mhPx + unmarks * hPx + idx * spacePx
            val height = if (idx % markedNumber == 0) mhPx else hPx
            VerticalLineUi(idx, offsetY + verticalPaddingPx, height)
        }

    }

    val textPaint = remember {
        android.graphics.Paint().apply {
            textAlign = android.graphics.Paint.Align.CENTER
            color = onBg.toArgb()
            setTypeface(typeface)
        }
    }
    val textMarginPx = with(density) { 16.dp.toPx() }

    var initialScrolled by rememberSaveable { mutableStateOf(false) }


    LaunchedEffect(lineDataList, viewportHeightPx, initialIndex) {
        if (!initialScrolled && viewportHeightPx > 0f && initialIndex in lineDataList.indices) {
            val target = (
                    lineDataList[initialIndex].offsetY +
                            lineDataList[initialIndex].height / 2f -
                            viewportHeightPx / 2f
                    ).toInt()
            scrollState.scrollTo(target)
            initialScrolled = true
        }
    }

    var middleIndex by rememberSaveable { mutableIntStateOf(0) }
    var middleOffset by rememberSaveable { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        snapshotFlow { middleIndex }.drop(1).collect { onMiddleItemChange(items[it]) }
    }

    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.isScrollInProgress to scrollState.value }
            .filter { (scrolling, _) -> !scrolling && initialScrolled }
            .debounce(60)
            .map { (_, off) -> off + middleOffset }
            .collectLatest { target ->
                scrollState.animateScrollTo(
                    target.toInt(),
                    spring(
                        Spring.DampingRatioLowBouncy,
                        Spring.StiffnessLow
                    )
                )
            }
    }

    val contentHeight = remember(lineDataList) {
        with(density) {
            val last = lineDataList.lastOrNull()
            ((last?.offsetY?.toDp() ?: 0.dp) + (last?.height?.toDp()
                ?: 0.dp) + verticalPaddingPx.toDp())
        }
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .onGloballyPositioned { viewportHeightPx = it.size.height.toFloat() }
            .width(120.dp)
            .verticalScroll(
                state = scrollState
            )
    ) {
        Canvas(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .width(VodovozWheelPickerDefaults.middleLineHeight)
                .height(contentHeight)
        ) {
            val centerY = scrollState.value + viewportHeightPx / 2f
            val maxDist = with(density) {
                ((VodovozWheelPickerDefaults.space + VodovozWheelPickerDefaults.lineWidth)
                        * 2 * markedNumber
                        + VodovozWheelPickerDefaults.markedLineWidth
                        ).toPx()
            }

            var best = Float.MAX_VALUE
            var bestDiff = 0f

            lineDataList.forEach { (i, offY, h) ->
                val lineCenter = offY + h / 2f
                val diff = lineCenter - centerY
                if (abs(diff) < best) {
                    best = abs(diff); bestDiff = diff; middleIndex = i
                }
            }
            middleOffset = bestDiff

            lineDataList.forEach { (i, offY, h) ->
                val isMarked = i % markedNumber == 0
                val isMiddle = i == middleIndex
                val widthPx = when {
                    isMiddle -> VodovozWheelPickerDefaults.middleLineHeight.toPx()
                    isMarked -> VodovozWheelPickerDefaults.markedLineHeight.toPx()
                    else -> VodovozWheelPickerDefaults.lineHeight.toPx()
                }
                val left = (size.width - widthPx) / 2f

                drawRect(
                    color = when {
                        centerY in offY..(offY + h) -> primaryColor
                        isMarked -> surfaceTint
                        else -> surfaceTint.copy(0.5f)
                    },
                    topLeft = Offset(left, offY),
                    size = Size(widthPx, h)
                )

                if (!isMarked) return@forEach

                val lineCenterY = offY + h / 2f
                val dist = abs(lineCenterY - centerY).coerceAtMost(maxDist)
                val t = 1f - dist / maxDist

                val textHeight = textPaint.descent() - textPaint.ascent()
                val textY = lineCenterY + textHeight / 2f - textPaint.descent()


                val textSize = with(density) { lerp(10.sp, 18.sp, t).toPx() }
                val alphaF = lerp(0.45f, 1f, t)

                textPaint.apply {
                    this.textSize = textSize
                    this.alpha = (alphaF * 255).toInt()
                }

                val textX = size.width - textMarginPx


                drawContext.canvas.nativeCanvas.drawText(
                    itemText(items[i]),
                    textX,
                    textY,
                    textPaint
                )
            }
        }
    }
}


@Preview
@Composable
private fun PickerViewVertical() {
    VodovozTheme {
        VodovozWheelPickerVertical(
            items = (-200..200).toList(),
            onMiddleItemChange = {}
        )
    }
}
