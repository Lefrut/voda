package com.vodovoz.app.design_system.modifiers

import android.graphics.Matrix
import android.graphics.RadialGradient
import android.graphics.Shader
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asComposePaint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import kotlin.math.max



fun Modifier.radialGradientBackground(
    centerFractionX: Float,
    centerFractionY: Float ,
    radiusFractionX: Float,
    radiusFractionY: Float,
    vararg colorsAndStops: Pair<Float, Color>,
): Modifier = this.then(
    Modifier.drawWithCache {
        val center = Offset(size.width * centerFractionX, size.height * centerFractionY)
        val radiusX = size.width * radiusFractionX
        val radiusY = size.height * radiusFractionY
        val baseRadius = max(radiusX, radiusY)


        val androidShader = RadialGradient(
            center.x,
            center.y,
            baseRadius,
            colorsAndStops.map { it.second.toArgb() }.toIntArray(),
            colorsAndStops.map { it.first }.toFloatArray(),
            Shader.TileMode.CLAMP
        ).apply {
            val matrix = Matrix().apply {
                setScale(radiusX / baseRadius, radiusY / baseRadius, center.x, center.y)
            }
            setLocalMatrix(matrix)
        }

        val paint = Paint().asFrameworkPaint().apply {
            shader = androidShader
        }

        onDrawWithContent {
            drawIntoCanvas { canvas ->
                canvas.drawRect(
                    0f, 0f, size.width, size.height,
                    paint.asComposePaint()
                )
            }
            drawContent()

        }
    }
)

