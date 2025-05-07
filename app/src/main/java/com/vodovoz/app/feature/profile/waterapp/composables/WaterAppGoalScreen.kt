package com.vodovoz.app.feature.profile.waterapp.composables

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.graphics.vector.toPath
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodovoz.app.R
import com.vodovoz.app.design_system.composables.button.VodovozButton
import kotlinx.coroutines.launch
import kotlin.random.Random
import android.graphics.Matrix as AndroidMatrix
import android.graphics.Path as AndroidPath


//TODO - complete this
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
            val vector = ImageVector.vectorResource(id = R.drawable.wawes)

            Image(
                painter = painterResource(id = R.drawable.wawes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()

            )

            BubblesUnderGround(
                imageVector = vector,
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
    val svgPaths = ImageVector.vectorResource(id = R.drawable.wawes)

    BubblesUnderGround(
        imageVector = svgPaths,
        bubbleCount = 40,
        modifier = Modifier.fillMaxSize()
    )
}


@Composable
fun BubblesUnderGround(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    bubbleCount: Int = 100,
) {
    val density = LocalDensity.current
    val canvasSize = remember { mutableStateOf(Size.Zero) }

    val svgComposePaths = remember(imageVector) {
        imageVector.toAndroidPaths().map { it.asComposePath() }
    }

    val clipPath = remember(svgComposePaths) {
        Path().apply { svgComposePaths.forEach { addPath(it) } }
    }

    @Immutable
    data class Bubble(var x: Float, var y: Float, val r: Float, val speed: Float, val color: Color)

    val bubbles = remember(bubbleCount) {
        List(bubbleCount) {
            val rDp = Random.nextFloat() * (25f - 6f) + 6f
            val speedDp = Random.nextFloat() * (100f - 30f) + 30f
            Bubble(
                x = Random.nextFloat(),
                y = 1f + Random.nextFloat(),
                r = with(density) { rDp.dp.toPx() },
                speed = with(density) { speedDp.dp.toPx() },
                color = if (Random.nextBoolean())
                    Color.Black
                else
                    Color.Blue
            )
        }
    }

    LaunchedEffect(canvasSize.value) {
        if (canvasSize.value == Size.Zero) return@LaunchedEffect
        var last = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            val dt = (now - last) / 1_000_000_000f
            last = now
            val h = canvasSize.value.height
            val w = canvasSize.value.width
            bubbles.forEach { b ->
                b.y -= b.speed * dt
                if (b.y * h + b.r < 0f) {
                    b.y = 1f + Random.nextFloat()
                    b.x = Random.nextFloat()
                }
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        canvasSize.value = size


        val r1 = 12.dp.toPx()
        val r2 = 18.dp.toPx()
        val r3 = 8.dp.toPx()

        val c1 = Offset(x = 100f, y = 100f)
        val c2 = Offset(x = size.width * 0.5f, y = size.height / 2)
        val c3 = Offset(x = size.width - 80f, y = size.height / 4)

        val color1 = Color.Red
        val color2 = Color.Green
        val color3 = Color.Yellow

        drawPath(
            path = clipPath,
            color = Color.Red,
            style = Stroke(width = 2.dp.toPx())
        )




        clipPath(clipPath, clipOp = ClipOp.Intersect){

            drawCircle(
                color = color1,
                radius = r1,
                center = Offset(x = 100f, y = -100f)
            )

            drawCircle(
                color = color1,
                radius = r1,
                center = c1
            )
            drawCircle(
                color = color2,
                radius = r2,
                center = c2
            )
            drawCircle(
                color = color3,
                radius = r3,
                center = c3
            )
        }

//            bubbles.forEach { b ->
//                drawCircle(
//                    color = b.color,
//                    radius = b.r,
//                    center = Offset(b.x * size.width, b.y * size.height)
//                )
//            }
    }
}


fun ImageVector.toAndroidPaths(): List<AndroidPath> {
    val result = mutableListOf<AndroidPath>()

    fun traverse(group: VectorGroup, currentMatrix: AndroidMatrix) {

        val matrix = AndroidMatrix(currentMatrix).apply {
            preScale(group.scaleX, group.scaleY)
            preTranslate(group.translationX, group.translationY)
            preRotate(group.rotation, group.pivotX, group.pivotY)
        }

        group.forEach { node ->
            when (node) {
                is VectorGroup -> traverse(node, matrix)
                is VectorPath -> {
                    val ap = AndroidPath().apply {
                        node.pathData.toPath(asComposePath())
                        transform(matrix)
                    }
                    result += ap
                }
            }

        }
    }
    traverse(this.root, AndroidMatrix())
    return result
}


@Preview
@Composable
private fun GradientWavesPreview() {
    DoubleInfiniteWaveFixedHeight()
}

@Composable
fun DoubleInfiniteWaveFixedHeight(
    modifier: Modifier = Modifier,
    waveAmplitude: Dp = 40.dp,
    waveColor1: Color = Color.Cyan,
    waveColor2: Color = Color.Blue,
) {
    val waveHeightPx = with(LocalDensity.current) { waveAmplitude.toPx() }
    val screenHeight =
        with(LocalDensity.current) { LocalConfiguration.current.screenHeightDp.dp.toPx() }
    val waveLength =
        with(LocalDensity.current) { LocalConfiguration.current.screenWidthDp.dp.toPx() }

    val progress1 = remember { Animatable(0f) }
    val progress2 = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            while (true) {
                progress1.animateTo(
                    waveLength,
                    animationSpec = infiniteRepeatable(
                        animation = tween(3000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )
                progress1.snapTo(0f)
            }
        }

        launch {
            while (true) {
                progress2.animateTo(
                    waveLength,
                    animationSpec = infiniteRepeatable(
                        animation = tween(3000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )
                progress2.snapTo(0f)
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        fun drawWave(offsetX: Float, isReversed: Boolean, color: Color, alpha: Float) {
            val path = Path()
            val startX = -waveLength + offsetX
            val baseY = screenHeight - waveHeightPx

            path.moveTo(startX, screenHeight)

            var x = startX
            while (x < size.width + waveLength) {
                val controlX = x + waveLength / 4
                val controlY = if (isReversed) baseY + waveHeightPx else baseY - waveHeightPx
                val endX = x + waveLength / 2

                path.quadraticBezierTo(controlX, controlY, endX, baseY)
                x += waveLength / 2
            }

            path.lineTo(size.width, screenHeight)
            path.lineTo(0f, screenHeight)
            path.close()

            drawPath(path, color = color.copy(alpha = alpha))
        }

        drawWave(progress1.value, isReversed = false, color = waveColor1, alpha = 1f)
        drawWave(progress2.value, isReversed = true, color = waveColor2, alpha = 0.5f)
    }
}
