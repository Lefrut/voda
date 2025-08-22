package com.vodovoz.app.design_system.modifiers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer

@Stable
fun Modifier.vodovozSurface(
    shape: Shape,
    backgroundColor: Color,
    border: BorderStroke?,
    shadowElevation: Float,
) = this
    .graphicsLayer(shadowElevation = shadowElevation, shape = shape, clip = false)
    .then(if (border != null) Modifier.border(border, shape) else Modifier)
    .background(color = backgroundColor, shape = shape)
    .clip(shape)
