package com.vodovoz.app.feature.profile.waterapp.composables

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodovoz.app.R
import com.vodovoz.app.design_system.composables.button.VodovozButton
import kotlinx.coroutines.launch


//TODO - complete this
@Composable
fun WaterAppGoalScreen(
    modifier: Modifier = Modifier,
    goal: Int,
    onCloseClick: () -> Unit,
    onStartClick: () -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {


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
                text = goal.toString(),
                color = MaterialTheme.colorScheme.background,
                style = MaterialTheme.typography.displayLarge
            )

            val bodyMedium = MaterialTheme.typography.bodyMedium
            Text(
                modifier = Modifier
                    .padding(top = 1.dp)
                    .height(25.dp)
                    .wrapContentSize(Alignment.Center),
                text = stringResource(R.string.ml),
                color = MaterialTheme.colorScheme.surfaceTint,
                style = bodyMedium.copy(lineHeight = bodyMedium.fontSize, letterSpacing = 0.sp)
            )

            Text(
                modifier = Modifier.padding(top = 32.dp),
                text = stringResource(id = R.string.vashe_pravilnoe_potreblenie),
                color = MaterialTheme.colorScheme.surfaceTint,
                style = bodyMedium.copy(textAlign = TextAlign.Center)
            )

            VodovozButton(
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
