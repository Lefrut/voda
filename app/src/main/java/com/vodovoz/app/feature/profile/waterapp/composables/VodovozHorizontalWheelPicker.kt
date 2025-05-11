package com.vodovoz.app.feature.profile.waterapp.composables

import android.graphics.Typeface
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.unit.sp
import com.vodovoz.app.R
import com.vodovoz.app.design_system.VodovozTheme
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlin.math.abs


@Immutable
data class HorizontalLineUi(
    val index: Int,
    val offsetX: Float,
    val width: Float
)


@Composable
fun <T : Any> VodovozWheelPicker(
    modifier: Modifier = Modifier,
    items: List<T>,
    initialIndex: Int = 0,
    markedNumber: Int = 5,
    itemText: (T) -> String = { item -> item.toString() },
    onMiddleItemChange: (T) -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(136.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Image(
            modifier = Modifier
                .padding(bottom = 17.dp)
                .fillMaxWidth()
                .height(82.dp),
            painter = painterResource(id = R.drawable.bg_horizontal_wheel),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.surfaceVariant.copy(0.5f))
        )

        VodovozWheelCore(
            modifier = Modifier.padding(bottom = 38.dp),
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
internal fun <T : Any> VodovozWheelCore(
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
    val resolver: FontFamily.Resolver = LocalFontFamilyResolver.current

    val typeface: Typeface = remember(resolver, style) {
        resolver.resolve(
            fontFamily = style.fontFamily,
            fontWeight = style.fontWeight ?: FontWeight.Normal,
            fontStyle = style.fontStyle ?: FontStyle.Normal,
            fontSynthesis = style.fontSynthesis ?: FontSynthesis.All,
        )
    }.value as Typeface

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceTintColor = MaterialTheme.colorScheme.surfaceTint
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground

    var viewportWidthPx by rememberSaveable { mutableFloatStateOf(0f) }

    val horizontalPaddingPx = viewportWidthPx / 1.2f

    val lineDataList = remember(items, viewportWidthPx) {
        if (viewportWidthPx == 0f) return@remember emptyList()

        val spacePx = with(density) { VodovozWheelPickerDefaults.space.toPx() }
        val wPx = with(density) { VodovozWheelPickerDefaults.lineWidth.toPx() }
        val mPx = with(density) { VodovozWheelPickerDefaults.markedLineWidth.toPx() }

        List(items.size) { idx ->
            val marksBefore = idx / markedNumber
            val unmarksBefore = idx - marksBefore
            val offsetX =
                marksBefore * mPx + unmarksBefore * wPx + idx * spacePx
            val width = if (idx % markedNumber == 0) mPx else wPx
            HorizontalLineUi(idx, offsetX + horizontalPaddingPx, width)
        }
    }

    val textPaint = remember {
        android.graphics.Paint().apply {
            textAlign = android.graphics.Paint.Align.CENTER
            color = onBackgroundColor.toArgb()
            setTypeface(typeface)
        }
    }

    var initialScrolled by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(lineDataList, viewportWidthPx, initialIndex) {
        if (!initialScrolled
            && viewportWidthPx > 0f
            && initialIndex in lineDataList.indices
        ) {

            val targetOffset = (
                    lineDataList[initialIndex].offsetX
                            + lineDataList[initialIndex].width / 2f
                            - viewportWidthPx / 2f
                    ).toInt()
            scrollState.scrollTo(targetOffset)
            initialScrolled = true
        }
    }

    val textRectHeightPx = with(density) { VodovozWheelPickerDefaults.textRectHeight.toPx() }
    val textMarginPx = with(density) { 16.dp.toPx() }


    var middleLineIndex by rememberSaveable { mutableIntStateOf(0) }
    var middleLineOffset by rememberSaveable { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        snapshotFlow { middleLineIndex }.drop(1).collect {
            onMiddleItemChange(items.elementAtOrNull(middleLineIndex) ?: return@collect)
        }
    }

    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.isScrollInProgress to scrollState.value }
            .filter { (isScrolling, _) -> !isScrolling && initialScrolled }
            .debounce(35)
            .map { (_, offset) -> offset + middleLineOffset }
            .collectLatest { target ->
                val targetInt = target.toInt()
                scrollState.animateScrollTo(
                    targetInt,
                    spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
    }

    val contentWidth = remember(lineDataList) {
        with(density) {
            val offsetX = lineDataList.lastOrNull()?.offsetX?.toDp() ?: 0.dp
            val width = lineDataList.lastOrNull()?.width?.toDp() ?: 0.dp

            offsetX + width + (horizontalPaddingPx * 2).toDp()
        }
    }


    Box(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { coords ->
                viewportWidthPx = coords.size.width.toFloat()
            }
            .height(120.dp)
            .horizontalScroll(scrollState)
            .requiredWidth(contentWidth)


    ) {
        Canvas(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .width(contentWidth)
                .height(VodovozWheelPickerDefaults.middleLineHeight)
        ) {
            val centerX = scrollState.value + viewportWidthPx / 2f

            val maxDist = with(density) {
                ((VodovozWheelPickerDefaults.space + VodovozWheelPickerDefaults.lineWidth)
                        * 2 * markedNumber
                        + VodovozWheelPickerDefaults.markedLineWidth
                        ).toPx()
            }

            var bestDist = Float.MAX_VALUE
            var bestDiff = 0f

            lineDataList.forEach { (idx, offsetX, width) ->
                val lineCenter = offsetX + width / 2f
                val diff = lineCenter - centerX
                if (abs(diff) < bestDist) {
                    bestDist = abs(diff)
                    bestDiff = diff
                    middleLineIndex = idx
                }
            }

            middleLineOffset = bestDiff

            lineDataList.forEach { (idx, offsetX, width) ->
                val isMarked = idx % markedNumber == 0
                val isMiddle = idx == middleLineIndex
                val lineHeight = when {
                    isMiddle -> VodovozWheelPickerDefaults.middleLineHeight
                    isMarked -> VodovozWheelPickerDefaults.markedLineHeight
                    else -> VodovozWheelPickerDefaults.lineHeight
                }.toPx()

                val top = (size.height - lineHeight) / 2f


                drawRect(
                    color = when {
                        centerX in offsetX..(offsetX + width) -> primaryColor
                        isMarked -> surfaceTintColor
                        else -> surfaceTintColor.copy(0.5f)
                    },
                    topLeft = Offset(offsetX, top),
                    size = Size(width, lineHeight)
                )


                if (!isMarked) return@forEach

                val lineCenterX = offsetX + width / 2f
                val dist = abs(lineCenterX - centerX).coerceAtMost(maxDist)

                val t = 1f - (dist / maxDist)

                val textSizePx = with(density) {
                    lerp(10.sp, 18.sp, t).toPx()
                }

                val alphaF = lerp(0.45f, 1f, t)

                textPaint.apply {
                    textSize = textSizePx
                    alpha = (alphaF * 255).toInt()
                }

                val textY = ((textRectHeightPx - textSizePx) / 2)

                items.getOrNull(idx)?.let { text ->
                    drawContext.canvas.nativeCanvas.drawText(
                        itemText(text),
                        lineCenterX,
                        -(textY + textMarginPx),
                        textPaint
                    )
                }

            }
        }
    }
}


data object VodovozWheelPickerDefaults {

    const val TAG = "VodovozWheelPicker"

    val lineWidth = 1.dp
    val markedLineWidth = 1.5.dp

    val lineHeight = 20.dp
    val markedLineHeight = 37.dp
    val middleLineHeight = 45.dp

    val space = 15.dp

    val textRectHeight = 45.dp

}

@Preview
@Composable
private fun PickerView() {
    VodovozTheme {


        VodovozWheelPicker(
            items = (-200..200).toList(),
            itemText = {
                it.toString()
            },
            onMiddleItemChange = {

            }
        )
    }
}
