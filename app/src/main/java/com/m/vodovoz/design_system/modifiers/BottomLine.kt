package com.m.vodovoz.design_system.modifiers

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
