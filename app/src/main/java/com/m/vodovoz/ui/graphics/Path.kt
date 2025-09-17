package com.m.vodovoz.ui.graphics

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.graphics.vector.toPath
import androidx.compose.ui.layout.ContentScale
import android.graphics.Matrix as AndroidMatrix
import android.graphics.Path as AndroidPath

fun ImageVector.toAndroidPaths(
    targetWidthPx: Float = defaultWidth.value,
    targetHeightPx: Float = defaultHeight.value,
    contentScale: ContentScale = ContentScale.Inside,
): List<AndroidPath> {
    val scaleFactor = contentScale.computeScaleFactor(
        srcSize = Size(defaultWidth.value, defaultHeight.value),
        dstSize = Size(targetWidthPx, targetHeightPx)
    )

    val scaleX = scaleFactor.scaleX
    val scaleY = scaleFactor.scaleY

    val scaledWidth = defaultWidth.value * scaleX
    val scaledHeight = defaultHeight.value * scaleY

    val dx = (targetWidthPx - scaledWidth) / 2f
    val dy = (targetHeightPx - scaledHeight) / 2f

    val result = mutableListOf<AndroidPath>()

    fun traverse(group: VectorGroup, currentMatrix: AndroidMatrix) {
        val matrix = AndroidMatrix(currentMatrix).apply {
            preScale(group.scaleX, group.scaleY)
            preTranslate(group.translationX, group.translationY)
            preRotate(group.rotation, group.pivotX, group.pivotY)
        }

        group.forEach { node ->
            when (node) {
                is VectorGroup -> traverse(node, matrix)
                is VectorPath -> {
                    val androidPath = AndroidPath().apply {
                        addPath(node.pathData.toPath().asAndroidPath())
                        transform(matrix)
                        transform(AndroidMatrix().apply {
                            setScale(scaleX, scaleY)
                            postTranslate(dx, dy)
                        })
                    }
                    result += androidPath
                }
            }
        }
    }

    traverse(root, AndroidMatrix())
    return result
}

fun List<Path>.mergeToSinglePath(pathOperation: PathOperation = PathOperation.Union): Path {
    val result = Path()
    if (isEmpty()) return result

    val merged = reduceOrNull { acc, path ->
        Path().apply {
            op(acc, path, pathOperation)
        }
    }

    if (merged != null) {
        result.addPath(merged)
    }

    return result
}

