package com.vodovoz.app.feature.profile.waterapp.composables

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodovoz.app.R
import com.vodovoz.app.design_system.robotoFontFamily
import com.vodovoz.app.feature.profile.waterapp.WaterAppHelper
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun WaterAppBottleScreen(
    maxLevel: Int,
    currentLevel: Int,
    changeWaterStep: Int,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onProgressChanged: (Float) -> Unit,
    onMinusClick: () -> Unit,
    onPlusClick: () -> Unit,
    onBottleClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.padding(top = 12.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back),
                tint = MaterialTheme.colorScheme.onBackground,
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .clip(MaterialTheme.shapes.small)
                    .clickable(onClick = onBackClick)
            )


            WaterProgressBar(
                modifier = Modifier.weight(1f),
                progress = currentLevel.toFloat() / maxLevel,
                onProgressChanged = onProgressChanged
            )

            Icon(
                painter = painterResource(R.drawable.ic_filter),
                tint = MaterialTheme.colorScheme.onBackground,
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .clip(MaterialTheme.shapes.small)
                    .clickable(onClick = onSettingsClick)
            )

        }


        val waterProgressAnnotatedString = buildAnnotatedString {
            withStyle(SpanStyle(color = WaterAppHelper.Colors.lightBlue)) {
                append(currentLevel.toString())
            }
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.surfaceTint)) {
                append(stringResource(R.string.splash))
                append(maxLevel.toString())
            }
        }

        Text(
            modifier = Modifier.padding(top = 4.dp),
            text = waterProgressAnnotatedString,
            style = MaterialTheme.typography.labelSmall
        )

        Spacer(Modifier.weight(0.56f))

        WaterAppBottle(
            modifier = Modifier,
            currentLevel = currentLevel,
            onClick = onBottleClick
        )

        Text(
            modifier = Modifier.padding(top = 20.dp),
            text = stringResource(R.string.click_on_bottle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.surfaceTint,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.weight(0.4f))

        Row(
            modifier = Modifier
                .padding(bottom = 43.dp)
                .width(165.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_minus),
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onMinusClick)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(4.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = stringResource(R.string.quantity_ml, changeWaterStep),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.15.sp)
            )

            Icon(
                painter = painterResource(R.drawable.ic_plus),
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onPlusClick)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(4.dp),
                tint = MaterialTheme.colorScheme.primary
            )

        }

    }
}


@Composable
private fun WaterAppBottle(modifier: Modifier = Modifier, currentLevel: Int, onClick: () -> Unit) {

    val scope = rememberCoroutineScope()
    val shakeOffset = remember { Animatable(0f) }


    Box(
        modifier = modifier
            .width(128.dp)
            .height(320.dp)
            .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
            .clickable(interactionSource = null, indication = null) {
                onClick()
                scope.launch {
                    shakeOffset.animateTo(
                        targetValue = 0f,
                        animationSpec = keyframes {
                            durationMillis = 200
                            0f at 0
                            10f at 100
                            (-10f) at 200
                            10f at 300
                            (-10f) at 400
                            0f at 500
                        }
                    )
                }
            }
    ) {
        Image(
            painter = painterResource(R.drawable.empty_bottle),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )



        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1.45f))
            Text(
                text = currentLevel.toString(),
                color = MaterialTheme.colorScheme.onBackground,
                style = TextStyle(
                    fontSize = 30.sp,
                    letterSpacing = 0.sp,
                    fontFamily = robotoFontFamily,
                    lineHeightStyle = LineHeightStyle(
                        LineHeightStyle.Alignment.Center,
                        LineHeightStyle.Trim.None
                    ),
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    ),
                    fontWeight = FontWeight.Medium
                )
            )

            val bodySmall = MaterialTheme.typography.bodySmall
            Text(
                modifier = Modifier
                    .wrapContentSize(Alignment.Center)
                    .width(40.dp)
                    .height(25.dp),
                text = stringResource(R.string.ml),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodySmall.copy(
                    lineHeight = bodySmall.fontSize,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(Modifier.weight(1.2f))
        }


    }
}


@Composable
private fun WaterProgressBar(
    modifier: Modifier = Modifier,
    progress: Float,
    onProgressChanged: (Float) -> Unit
) {
    val density = LocalDensity.current
    var barWidthPx by remember { mutableFloatStateOf(0f) }
    val barWidthDp = with(density) { barWidthPx.toDp() }
    val iconSize = 26.dp
    val minBarWidthPx = with(density) { iconSize.toPx() + 4.dp.toPx() }

    var internalProgress by remember(progress) { mutableFloatStateOf(progress.coerceIn(0f, 1f)) }
    val animatedProgress by animateFloatAsState(
        targetValue = internalProgress,
        animationSpec = tween(durationMillis = 40, easing = LinearEasing),
        label = "progressAnimation"
    )

    var dragOffset by remember { mutableFloatStateOf(0f) }


    LaunchedEffect(internalProgress, barWidthPx) {
        if (minBarWidthPx > barWidthPx) return@LaunchedEffect

        val p = progress.coerceIn(0f, 1f)
        dragOffset =
            (p * (barWidthPx - minBarWidthPx) + minBarWidthPx).coerceIn(minBarWidthPx, barWidthPx)
    }

    val draggableState = rememberDraggableState { delta ->
        val newOffset = (dragOffset + delta).coerceIn(minBarWidthPx, barWidthPx)
        internalProgress =
            ((newOffset - minBarWidthPx) / (barWidthPx - minBarWidthPx)).coerceIn(0f, 1f)

        onProgressChanged(internalProgress)
    }

    Box(
        modifier = modifier
            .height(32.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(100.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(100.dp))
            .onGloballyPositioned { coords ->
                val newBarWidthPx = coords.size.width.toFloat()
                if (barWidthPx != newBarWidthPx) {
                    barWidthPx = newBarWidthPx
                    dragOffset = internalProgress * (barWidthPx - minBarWidthPx) + minBarWidthPx
                }
            }
    ) {


        Box(
            modifier = Modifier
                .fillMaxHeight()
                .background(
                    brush = Brush.linearGradient(
                        *arrayOf(
                            0.21f to WaterAppHelper.Colors.lightBlue,
                            1.28f to MaterialTheme.colorScheme.background
                        ),
                        start = Offset.Infinite,
                        end = Offset.Zero,
                    ),
                    shape = RoundedCornerShape(100.dp)
                )
                .width((barWidthDp * animatedProgress).coerceAtLeast(iconSize + 4.dp)),
            contentAlignment = Alignment.CenterEnd
        ) {
            Image(
                painter = painterResource(R.drawable.ic_water_thumb),
                contentDescription = null,
                modifier = Modifier
                    .draggable(
                        orientation = Orientation.Horizontal,
                        state = draggableState,
                    )
                    .padding(horizontal = 4.dp)
                    .size(iconSize)

            )
        }
    }
}
