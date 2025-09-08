package com.vodovoz.app.feature.profile.waterapp.composables

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith

fun goalCompletedTransition(): ContentTransform =
    fadeIn(
        animationSpec = tween(
            durationMillis = 300,
            delayMillis = 0,
            easing = LinearEasing
        ),
        initialAlpha = 0.6f
    ) togetherWith fadeOut(
        animationSpec = tween(
            durationMillis = 300,
            delayMillis = 0,
            easing = LinearEasing
        ),
        targetAlpha = 0f
    )

fun waterAppTransition(): ContentTransform =
    (fadeIn(
        animationSpec = tween(
            durationMillis = 220,
            delayMillis = 90
        )
    ) + scaleIn(
        initialScale = 0.92f,
        animationSpec = tween(
            durationMillis = 220,
            delayMillis = 90
        )
    )) togetherWith fadeOut(
        animationSpec = tween(
            durationMillis = 90
        )
    )