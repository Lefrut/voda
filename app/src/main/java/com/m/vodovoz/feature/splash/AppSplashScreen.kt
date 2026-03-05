package com.m.vodovoz.feature.splash

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.m.vodovoz.R
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.composables.placeholders.ErrorPlaceholderMode
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.PlaceholderType
import com.m.vodovoz.feature.splash.model.SplashState
import com.m.vodovoz.feature.splash.model.SplashUiState

@Composable
fun AppSplashScreen(viewModel: SplashViewModel, viewState: SplashState) {
    Box(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
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
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
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


