package com.vodovoz.app.ui.insets.ime

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import kotlin.math.max

fun View.handleImeInsetIfNeeded() {
    var originalPaddingBottom: Int? = null
    ViewCompat.setWindowInsetsAnimationCallback(
        this,
        object : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_STOP) {

            override fun onStart(
                animation: WindowInsetsAnimationCompat,
                bounds: WindowInsetsAnimationCompat.BoundsCompat,
            ): WindowInsetsAnimationCompat.BoundsCompat {
                if (originalPaddingBottom == null) {
                    originalPaddingBottom = this@handleImeInsetIfNeeded.paddingBottom
                }
                return bounds
            }

            override fun onProgress(
                insets: WindowInsetsCompat,
                runningAnimations: MutableList<WindowInsetsAnimationCompat>,
            ): WindowInsetsCompat {
                runningAnimations
                    .find { it.typeMask and WindowInsetsCompat.Type.ime() != 0 }
                    ?: return insets

                val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                this@handleImeInsetIfNeeded.updatePadding(
                    bottom = max(imeHeight, originalPaddingBottom ?: 0),
                )

                return insets
            }
        }
    )
}

fun View.removeImeHandling() {
    ViewCompat.setWindowInsetsAnimationCallback(this, null)
}