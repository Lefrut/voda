package com.vodovoz.app.feature.splash

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.vodovoz.app.R
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.composables.placeholders.ErrorPlaceholderMode
import com.vodovoz.app.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.vodovoz.app.design_system.composables.placeholders.PlaceholderType
import com.vodovoz.app.feature.splash.model.SplashState
import com.vodovoz.app.feature.splash.model.SplashUiState

@Composable
fun AppSplashScreen(viewModel: SplashViewModel, viewState: SplashState) {
    //todo - when will be animation
//    val context = LocalContext.current
//    val composition = rememberLottieComposition(
//        LottieCompositionSpec.File(viewState.filePath)
//
//    )
//
//    val lottieAnimationState = animateLottieCompositionAsState(
//        composition = composition.value,
//        iterations = LottieConstants.IterateForever
//    )


    Box(modifier = Modifier.fillMaxSize()) {
        when (viewState.uiState) {
            SplashUiState.Error -> {
                NetworkErrorPlaceholder(
                    mode = ErrorPlaceholderMode.Fixed(PlaceholderType.NetworkError),
                    onTryAgainClick = {
                        viewModel.refreshApp(viewState.filePath.isNotBlank())
                    }
                )
            }

            SplashUiState.Animation -> {
                SplashPlaceholder()
                //todo - when will be animation
//                LottieAnimation(
//                    composition = composition.value,
//                    progress = { lottieAnimationState.progress },
//                    modifier = Modifier.fillMaxSize(),
//                    contentScale = ContentScale.FillBounds
//                )
//
//                if (!lottieAnimationState.isPlaying || context.isTablet()) {
//                    SplashPlaceholder()
//                }
//
//                LaunchedEffect(Unit) {
//                    snapshotFlow { lottieAnimationState.isPlaying }.filter { isPlaying ->
//                        isPlaying
//                    }.take(1).collect {
//                        viewModel.hideAndroidSplash()
//                    }
//                }
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
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )


    Image(
        painter = painterResource(id = R.drawable.ic_splash),
        contentDescription = null,
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        contentScale = ContentScale.Crop
    )
}

@Preview(apiLevel = 34)
@Composable
private fun SplashPlaceholderPreview() {
    VodovozTheme {
        SplashPlaceholder()
    }
}


