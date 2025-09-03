package com.vodovoz.app.design_system.composables.blur

import android.graphics.Bitmap
import android.graphics.Picture
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.requireGraphicsContext
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skydoves.cloudy.cloudy
import com.vodovoz.app.R
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


@Composable
inline fun BlurBox(
    modifier: Modifier = Modifier,
    showBlur: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val blurRadius = 24.dp
    val density = LocalDensity.current

    val blurModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && showBlur) {
        Modifier.blur(blurRadius, blurRadius)
    } else if (Build.VERSION.SDK_INT >= 28) {
        Modifier.cloudy(with(density) { blurRadius.roundToPx() })
    } else {
        Modifier.drawWithContent {
            drawContent()
            drawRect(Color.White.copy(0.97f))
        }
    }

    Box(
        modifier = modifier
            .then(
                if (showBlur) blurModifier
                else Modifier
            )
    ) {
        content()
    }
}

@Composable
inline fun VodovozBlur(
    modifier: Modifier = Modifier,
    showBlur: Boolean = true,
    text: String = "",
    textStyle: TextStyle = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp),
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        BlurBox(showBlur = showBlur) {
            content()
        }
        if (showBlur) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_no_visibility),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = text,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = textStyle,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

fun Modifier.vodovozBlur(radius: Dp = 14.dp): Modifier {
    return then(BlurModifierNodeElement(radius))
}

private data class BlurModifierNodeElement(
    val radius: Dp = 14.dp,
) : ModifierNodeElement<BlurModifierNode>() {

    override fun InspectorInfo.inspectableProperties() {
        name = "vodovoz blur"
        properties["vodovoz blur"] = radius
    }

    override fun create(): BlurModifierNode = BlurModifierNode(
        radius = radius,
    )

    override fun update(node: BlurModifierNode) {
        node.radius = radius
    }
}


private class BlurModifierNode(
    var radius: Dp,
) : DrawModifierNode, Modifier.Node() {

    private var cachedOutput: Bitmap? by mutableStateOf(null)

    override fun ContentDrawScope.draw() {
        val graphicsLayer = requireGraphicsContext().createGraphicsLayer()

        graphicsLayer.record {
            this@draw.drawContent()
        }

        drawLayer(graphicsLayer)

        coroutineScope.launch {
            try {
                val targetBitmap: Bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                    .copy(Bitmap.Config.ARGB_8888, true)

                val out =
                    if (cachedOutput == null || cachedOutput?.width != targetBitmap.width || cachedOutput?.height != targetBitmap.height) {
                        createCompatibleBitmap(targetBitmap).fastblur(radius.roundToPx())?.apply {
                            drawImage(this.asImageBitmap())
                        }
                            ?: throw RuntimeException("Couldn't capture a bitmap from the composable tree")
                    } else {
                        cachedOutput!!
                    }

                drawImage(out.asImageBitmap())

            } catch (_: Exception) {
            } finally {
                requireGraphicsContext().releaseGraphicsLayer(graphicsLayer)
            }
        }
    }

}

private fun createCompatibleBitmap(inputBitmap: Bitmap) =
    Bitmap.createBitmap(inputBitmap.width, inputBitmap.height, inputBitmap.config!!)

fun Bitmap.fastblur(radius: Int, scale: Float = 0.5f): Bitmap? {
    var sentBitmap = this
    val width = Math.round(sentBitmap.width * scale)
    val height = Math.round(sentBitmap.height * scale)
    sentBitmap = Bitmap.createScaledBitmap(sentBitmap, width, height, false)

    val bitmap = sentBitmap.copy(sentBitmap.config!!, true)

    if (radius < 1) {
        return (null)
    }

    val w = bitmap.width
    val h = bitmap.height

    val pix = IntArray(w * h)
    bitmap.getPixels(pix, 0, w, 0, 0, w, h)

    val wm = w - 1
    val hm = h - 1
    val wh = w * h
    val div = radius + radius + 1

    val r = IntArray(wh)
    val g = IntArray(wh)
    val b = IntArray(wh)
    var rsum: Int
    var gsum: Int
    var bsum: Int
    var x: Int
    var y: Int
    var i: Int
    var p: Int
    var yp: Int
    var yi: Int
    val vmin = IntArray(max(w.toDouble(), h.toDouble()).toInt())

    var divsum = (div + 1) shr 1
    divsum *= divsum
    val dv = IntArray(256 * divsum)
    i = 0
    while (i < 256 * divsum) {
        dv[i] = (i / divsum)
        i++
    }

    yi = 0
    var yw = yi

    val stack = Array(div) {
        IntArray(
            3
        )
    }
    var stackpointer: Int
    var stackstart: Int
    var sir: IntArray
    var rbs: Int
    val r1 = radius + 1
    var routsum: Int
    var goutsum: Int
    var boutsum: Int
    var rinsum: Int
    var ginsum: Int
    var binsum: Int

    y = 0
    while (y < h) {
        bsum = 0
        gsum = bsum
        rsum = gsum
        boutsum = rsum
        goutsum = boutsum
        routsum = goutsum
        binsum = routsum
        ginsum = binsum
        rinsum = ginsum
        i = -radius
        while (i <= radius) {
            p = pix[(yi + min(wm.toDouble(), max(i.toDouble(), 0.0))).toInt()]
            sir = stack[i + radius]
            sir[0] = (p and 0xff0000) shr 16
            sir[1] = (p and 0x00ff00) shr 8
            sir[2] = (p and 0x0000ff)
            rbs = (r1 - abs(i.toDouble())).toInt()
            rsum += sir[0] * rbs
            gsum += sir[1] * rbs
            bsum += sir[2] * rbs
            if (i > 0) {
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
            } else {
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
            }
            i++
        }
        stackpointer = radius

        x = 0
        while (x < w) {
            r[yi] = dv[rsum]
            g[yi] = dv[gsum]
            b[yi] = dv[bsum]

            rsum -= routsum
            gsum -= goutsum
            bsum -= boutsum

            stackstart = stackpointer - radius + div
            sir = stack[stackstart % div]

            routsum -= sir[0]
            goutsum -= sir[1]
            boutsum -= sir[2]

            if (y == 0) {
                vmin[x] = min((x + radius + 1).toDouble(), wm.toDouble()).toInt()
            }
            p = pix[yw + vmin[x]]

            sir[0] = (p and 0xff0000) shr 16
            sir[1] = (p and 0x00ff00) shr 8
            sir[2] = (p and 0x0000ff)

            rinsum += sir[0]
            ginsum += sir[1]
            binsum += sir[2]

            rsum += rinsum
            gsum += ginsum
            bsum += binsum

            stackpointer = (stackpointer + 1) % div
            sir = stack[stackpointer % div]

            routsum += sir[0]
            goutsum += sir[1]
            boutsum += sir[2]

            rinsum -= sir[0]
            ginsum -= sir[1]
            binsum -= sir[2]

            yi++
            x++
        }
        yw += w
        y++
    }
    x = 0
    while (x < w) {
        bsum = 0
        gsum = bsum
        rsum = gsum
        boutsum = rsum
        goutsum = boutsum
        routsum = goutsum
        binsum = routsum
        ginsum = binsum
        rinsum = ginsum
        yp = -radius * w
        i = -radius
        while (i <= radius) {
            yi = (max(0.0, yp.toDouble()) + x).toInt()

            sir = stack[i + radius]

            sir[0] = r[yi]
            sir[1] = g[yi]
            sir[2] = b[yi]

            rbs = (r1 - abs(i.toDouble())).toInt()

            rsum += r[yi] * rbs
            gsum += g[yi] * rbs
            bsum += b[yi] * rbs

            if (i > 0) {
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
            } else {
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
            }

            if (i < hm) {
                yp += w
            }
            i++
        }
        yi = x
        stackpointer = radius
        y = 0
        while (y < h) {
            pix[yi] = (-0x1000000 and pix[yi]) or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]

            rsum -= routsum
            gsum -= goutsum
            bsum -= boutsum

            stackstart = stackpointer - radius + div
            sir = stack[stackstart % div]

            routsum -= sir[0]
            goutsum -= sir[1]
            boutsum -= sir[2]

            if (x == 0) {
                vmin[y] = (min((y + r1).toDouble(), hm.toDouble()) * w).toInt()
            }
            p = x + vmin[y]

            sir[0] = r[p]
            sir[1] = g[p]
            sir[2] = b[p]

            rinsum += sir[0]
            ginsum += sir[1]
            binsum += sir[2]

            rsum += rinsum
            gsum += ginsum
            bsum += binsum

            stackpointer = (stackpointer + 1) % div
            sir = stack[stackpointer]

            routsum += sir[0]
            goutsum += sir[1]
            boutsum += sir[2]

            rinsum -= sir[0]
            ginsum -= sir[1]
            binsum -= sir[2]

            yi += w
            y++
        }
        x++
    }

    bitmap.setPixels(pix, 0, w, 0, 0, w, h)

    return (bitmap)
}