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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.m.vodovoz.design_system.composables.image.VodovozAsyncImage
import com.m.vodovoz.domain.general.model.promotion.FloatingPromoButtonModel
import kotlin.math.roundToInt

object FloatingPromoBannerDefaults {
    val BannerSize = 100.dp
    val HorizontalInset = 16.dp
    val DefaultBottomInset = 64.dp
    val ProductButtonSpacing = 60.dp

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
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val bannerSizePx = with(density) { FloatingPromoBannerDefaults.BannerSize.roundToPx() }
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

    LaunchedEffect(isVisible) {
        visibilityState.targetState = isVisible
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(FloatingPromoBannerDefaults.BannerSize),
    ) {
        val availableHorizontalSpacePx = (
                constraints.maxWidth - bannerSizePx - horizontalInsetPx * 2
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
            VodovozAsyncImage(
                model = button.imageUrl,
                contentDescription = button.name,
                modifier = Modifier
                    .size(FloatingPromoBannerDefaults.BannerSize)
                    .clickable(
                        role = Role.Button,
                        onClick = onClick,
                        indication = null,
                        interactionSource = null,
                    ),
            )
        }
    }
}
