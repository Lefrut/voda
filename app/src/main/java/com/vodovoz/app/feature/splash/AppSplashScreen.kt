package com.vodovoz.app.feature.splash

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.vodovoz.app.R
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.vodovoz.app.feature.splash.model.SplashState
import com.vodovoz.app.feature.splash.model.SplashUiState
import com.vodovoz.app.util.extensions.isTablet
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take

@Composable
fun AppSplashScreen(viewModel: SplashViewModel, viewState: SplashState) {
    val context = LocalContext.current
    val composition by rememberLottieComposition(
        LottieCompositionSpec.File(viewState.filePath)
    )

    val lottieAnimationState = animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )


    Box(modifier = Modifier.fillMaxSize()) {
        when (viewState.uiState) {
            SplashUiState.Error -> {
                NetworkErrorPlaceholder {
                    viewModel.refreshApp(viewState.filePath.isNotBlank())
                }
            }

            SplashUiState.Animation -> {
                LottieAnimation(
                    composition = composition,
                    progress = { lottieAnimationState.progress },
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )

                if (!lottieAnimationState.isPlaying || context.isTablet()) {
                    SplashPlaceholder()
                }

                LaunchedEffect(Unit) {
                    snapshotFlow { lottieAnimationState.isPlaying }.filter { isPlaying ->
                        isPlaying
                    }.take(1).collect {
                        viewModel.hideAndroidSplash()
                    }
                }
            }

            SplashUiState.Placeholder -> {
                SplashPlaceholder()
            }
        }
    }
}

@Composable
private fun SplashPlaceholder(modifier: Modifier = Modifier) {

    val infiniteTransition = rememberInfiniteTransition(label = "infiniteTransition")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(2.3f))
        Image(
            painter = painterResource(id = R.drawable.ic_logo_splash),
            contentDescription = null,
            modifier = Modifier
                .size(134.dp, 126.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.background)
        )

        Spacer(modifier = Modifier.weight(2.84f))
    }
}

@Preview(apiLevel = 34)
@Composable
private fun SplashPlaceholderPreview() {
    VodovozTheme {
        SplashPlaceholder()
    }
}


