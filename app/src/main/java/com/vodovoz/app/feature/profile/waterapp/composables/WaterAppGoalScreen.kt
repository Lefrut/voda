package com.vodovoz.app.feature.profile.waterapp.composables

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.graphics.vector.toPath
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodovoz.app.R
import com.vodovoz.app.design_system.composables.button.VodovozButton
import com.vodovoz.app.ui.canvas.mergeToSinglePath
import com.vodovoz.app.ui.canvas.toAndroidPaths
import kotlin.random.Random
import android.graphics.Matrix as AndroidMatrix
import android.graphics.Path as AndroidPath

@Composable
fun WaterAppGoalScreen(
    modifier: Modifier = Modifier,
    goal: Int,
    onCloseClick: () -> Unit,
    onStartClick: () -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            Modifier
                .padding(top = 140.dp)
                .matchParentSize()
        ) {
            val vector = ImageVector.vectorResource(id = R.drawable.waves)

            val infiniteTransition = rememberInfiniteTransition(label = "waves")

            val offsetX by infiniteTransition.animateFloat(
                initialValue = -20f,
                targetValue = 20f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "offsetX"
            )

            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 3200, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Image(
                painter = painterResource(id = R.drawable.waves),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        translationX = offsetX
                        scaleX = scale
                        scaleY = scale
                    }
            )


            BubblesClipped(
                clipVector = vector,
                modifier = Modifier.matchParentSize()
            )
        }

        Column(
            modifier = modifier.matchParentSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.icon_close),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(16.dp)
                    .size(24.dp)
                    .clickable(onClick = onCloseClick)
            )
            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = stringResource(id = R.string.vasha_norma),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineMedium.copy(lineHeight = 26.sp)
            )

            Text(
                modifier = Modifier.padding(top = 186.dp),
                text = goal.toString(),
                color = MaterialTheme.colorScheme.background,
                style = MaterialTheme.typography.displayLarge
            )

            val bodyMedium = MaterialTheme.typography.bodyMedium
            Text(
                modifier = Modifier
                    .padding(top = 1.dp)
                    .height(25.dp)
                    .wrapContentHeight(Alignment.CenterVertically),
                text = stringResource(R.string.ml),
                color = MaterialTheme.colorScheme.background,
                style = bodyMedium.copy(lineHeight = bodyMedium.fontSize, letterSpacing = 0.sp)
            )

            Text(
                modifier = Modifier.padding(top = 32.dp),
                text = stringResource(id = R.string.vashe_pravilnoe_potreblenie),
                color = MaterialTheme.colorScheme.primaryContainer,
                style = bodyMedium.copy(textAlign = TextAlign.Center)
            )

            Spacer(modifier = Modifier.weight(1f))

            VodovozButton(
                modifier = Modifier.padding(
                    bottom = 32.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                text = stringResource(id = R.string.start),
                onClick = onStartClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Preview
@Composable
fun Demo() {
    val svgPaths = ImageVector.vectorResource(id = R.drawable.waves)

    BubblesClipped(
        clipVector = svgPaths,
        modifier = Modifier.fillMaxSize()
    )
}


@Composable
fun BubblesClipped(
    clipVector: ImageVector,
    modifier: Modifier = Modifier,
    bubbleCount: Int = 65,
) {
    val density = LocalDensity.current
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    val svgComposePaths = remember(clipVector, canvasSize) {
        clipVector.toAndroidPaths(
            targetWidthPx = canvasSize.width,
            targetHeightPx = canvasSize.height,
            contentScale = ContentScale.Crop
        ).map { path -> path.asComposePath() }
    }

    val clipPath = remember(svgComposePaths, canvasSize) {
        svgComposePaths.mergeToSinglePath()
    }

    @Immutable
    data class Bubble(
        val startX: Float,
        val baseSizePx: Float,
        val startYOffset: Float,
        val durationMillis: Int,
        val delayMillis: Int,
        val color: Color
    )

    val bubbles = remember(canvasSize, bubbleCount) {
        val minWhiteSize = with(density) { 4.dp.toPx() }
        val maxWhiteSize = with(density) { 12.dp.toPx() }
        val minBlueSize = with(density) { 6.dp.toPx() }
        val maxBlueSize = with(density) { 14.dp.toPx() }

        List(bubbleCount) {
            val isBlue = Random.nextFloat() < 0.2f
            val color = if (isBlue) {
                Color(0xFF05A4FF)
            } else {
                Color.White
            }.copy(alpha = 0.5f)

            val size = if (isBlue) {
                Random.nextFloat() * (maxBlueSize - minBlueSize) + minBlueSize
            } else {
                Random.nextFloat() * (maxWhiteSize - minWhiteSize) + minWhiteSize
            }

            Bubble(
                startX = Random.nextFloat() * canvasSize.width,
                baseSizePx = size,
                startYOffset = with(density) { 20.dp.toPx() },
                durationMillis = Random.nextInt(3000, 6000),
                delayMillis = Random.nextInt(0, 3000),
                color = color
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "bubbles")

    val animatedProgress = bubbles.mapIndexed { index, bubble ->
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = bubble.durationMillis,
                    delayMillis = bubble.delayMillis,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "bubble-progress-$index"
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        canvasSize = size

        clipPath(path = clipPath, clipOp = ClipOp.Intersect) {
            bubbles.forEachIndexed { index, bubble ->
                val progress = animatedProgress[index].value

                val y =
                    canvasSize.height + bubble.startYOffset - (canvasSize.height + bubble.startYOffset + bubble.baseSizePx) * progress
                val radius = bubble.baseSizePx * (1f - 0.6f * progress)
                val alpha = 0.5f * (1f - progress)

                drawCircle(
                    color = bubble.color.copy(alpha = alpha),
                    radius = radius,
                    center = Offset(bubble.startX, y)
                )
            }
        }
    }
}