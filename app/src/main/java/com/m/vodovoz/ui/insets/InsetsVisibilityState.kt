package com.m.vodovoz.ui.insets

import android.view.View
import androidx.compose.runtime.Stable
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat.Type.InsetsType
import androidx.core.view.updatePadding
import kotlinx.coroutines.flow.StateFlow

@Stable
interface InsetsVisibilityState {

    val statusBarInsets: StateFlow<InsetsState>

    val navigationBarInsets: StateFlow<InsetsState>

    fun consumeStatusBarInsets(consume: Boolean)

    fun consumeNavigationBarInsets(consume: Boolean)

    fun consumeSystemBarInsets(
        consume: Boolean,
    ) {
        consumeStatusBarInsets(consume)
        consumeNavigationBarInsets(consume)
    }

    val imeInsets: StateFlow<InsetsState>

    fun consumeIme(consume: Boolean)

    val insets: List<StateFlow<InsetsState>>
        get() = listOf(statusBarInsets, navigationBarInsets, imeInsets)


}

data class InsetsState(
    val consume: Boolean,
    @InsetsType
    val type: Int,
)


data class InsetsPadding(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
)

operator fun InsetsPadding.plus(other: InsetsPadding): InsetsPadding {
    return InsetsPadding(
        left = this.left + other.left,
        top = this.top + other.top,
        right = this.right + other.right,
        bottom = this.bottom + other.bottom
    )
}

fun Insets.toInsetsPadding(): InsetsPadding {
    return InsetsPadding(left, top, right, bottom)
}

fun View.updatePadding(
    insetsPadding: InsetsPadding,
) = updatePadding(
    left = insetsPadding.left,
    top = insetsPadding.top,
    right = insetsPadding.right,
    bottom = insetsPadding.bottom
)

