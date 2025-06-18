package com.vodovoz.app.design_system.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.isElementVisible(onVisibilityChanged: (Boolean) -> Unit) = composed {
    val isVisible by remember { derivedStateOf { mutableStateOf(false) } }
    LaunchedEffect(isVisible.value) { onVisibilityChanged.invoke(isVisible.value) }

    onGloballyPositioned { layoutCoordinates ->
        isVisible.value = layoutCoordinates.parentLayoutCoordinates?.let {
            val parentBounds = it.boundsInWindow()
            val childBounds = layoutCoordinates.boundsInWindow()
            parentBounds.overlaps(childBounds)
        } ?: false
    }
}

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


fun Modifier.bottomLine(color: Color, thickness: Dp = 1.dp): Modifier = this.then(
    Modifier.drawWithContent {
        val y = size.height - thickness.toPx() / 2

        drawContent()
        drawLine(
            color = color,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = thickness.toPx()
        )
    }
)

fun Modifier.detectTap(onClick: () -> Unit): Modifier {
    return pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown(pass = PointerEventPass.Initial)
            waitForUpOrCancellation(pass = PointerEventPass.Initial)
            onClick()
        }
    }
}