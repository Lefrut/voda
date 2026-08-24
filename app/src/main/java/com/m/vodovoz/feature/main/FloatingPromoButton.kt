package com.m.vodovoz.feature.main

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.m.vodovoz.R
import com.m.vodovoz.design_system.composables.image.VodovozAsyncImage
import com.m.vodovoz.domain.general.model.promotion.FloatingPromoButtonModel
import kotlin.math.roundToInt

object FloatingPromoBannerDefaults {
    val BannerWidth = 100.dp
    val BannerHeight = 100.dp
    val HorizontalInset = 16.dp
    val DefaultBottomInset = 64.dp
    val ProductButtonSpacing = 60.dp
    val CloseButtonSize = 24.dp
    val CloseButtonTouchSize = 32.dp
    val CloseButtonTrailingOffset = 0.dp

    const val PositionAnimationDurationMillis = 220
    const val HideAnimationDurationMillis = 180
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun FloatingPromoButton(
    button: FloatingPromoButtonModel,
    isVisible: Boolean,
    side: FloatingPromoSide,
    onClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val bannerWidth = button.width?.dp ?: FloatingPromoBannerDefaults.BannerWidth
    val bannerHeight = button.height?.dp ?: FloatingPromoBannerDefaults.BannerHeight
    val bannerWidthPx = with(density) { bannerWidth.roundToPx() }
    val horizontalInsetPx = with(density) {
        FloatingPromoBannerDefaults.HorizontalInset.roundToPx()
    }
    val sideProgress by animateFloatAsState(
        targetValue = if (side == FloatingPromoSide.Left) 0f else 1f,
        animationSpec = tween(
            durationMillis = FloatingPromoBannerDefaults.PositionAnimationDurationMillis,
            easing = FastOutSlowInEasing,
        ),
        label = "floatingPromoSide",
    )
    val visibilityState = remember(button.id) { MutableTransitionState(false) }
    var isImageLoaded by remember(button.id, button.imageUrl) { mutableStateOf(false) }

    LaunchedEffect(isVisible) {
        visibilityState.targetState = isVisible
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(bannerHeight),
    ) {
        val availableHorizontalSpacePx = (
                constraints.maxWidth - bannerWidthPx - horizontalInsetPx * 2
                ).coerceAtLeast(0)
        val horizontalOffsetPx = horizontalInsetPx +
                (availableHorizontalSpacePx * sideProgress).roundToInt()

        AnimatedVisibility(
            visibleState = visibilityState,
            modifier = Modifier.offset { IntOffset(horizontalOffsetPx, 0) },
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = FloatingPromoBannerDefaults.PositionAnimationDurationMillis,
                    easing = FastOutSlowInEasing,
                )
            ),
            exit = fadeOut(
                animationSpec = tween(
                    durationMillis = FloatingPromoBannerDefaults.HideAnimationDurationMillis,
                    easing = FastOutSlowInEasing,
                )
            ),
        ) {
            Box(modifier = Modifier.size(width = bannerWidth, height = bannerHeight)) {
                VodovozAsyncImage(
                    model = button.imageUrl,
                    contentDescription = button.name,
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(
                            enabled = isImageLoaded,
                            role = Role.Button,
                            onClick = onClick,
                            indication = null,
                            interactionSource = null,
                        ),
                    onLoading = { isImageLoaded = false },
                    onSuccess = { isImageLoaded = true },
                    onError = { isImageLoaded = false },
                )

                if (isImageLoaded) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = FloatingPromoBannerDefaults.CloseButtonTrailingOffset)
                            .size(FloatingPromoBannerDefaults.CloseButtonTouchSize)
                            .clip(CircleShape)
                            .clickable(
                                role = Role.Button,
                                onClick = onCloseClick,
                            ),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close_circle),
                            contentDescription = stringResource(R.string.close_floating_promo),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(FloatingPromoBannerDefaults.CloseButtonSize),
                            tint = MaterialTheme.colorScheme.surfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
