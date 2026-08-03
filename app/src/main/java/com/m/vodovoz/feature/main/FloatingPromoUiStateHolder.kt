package com.m.vodovoz.feature.main

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.max

internal class FloatingPromoUiStateHolder(
    private val clickDebounceMillis: Long,
) {
    var destinationId: Int? = null
        private set

    var isBottomNavigationVisible: Boolean = true
        private set

    var isSuppressed: Boolean = false
        private set

    private var dynamicExtraBottomOffsetPx: Int? = null
    private var lastClickAt: Long? = null

    var presentation by mutableStateOf(FloatingPromoPresentation())
        private set

    val isPromoVisible: Boolean
        get() = presentation.isVisible

    fun onDestinationChanged(destinationId: Int) {
        this.destinationId = destinationId
        dynamicExtraBottomOffsetPx = null
        isSuppressed = false
    }

    fun setBottomNavigationVisible(visible: Boolean) {
        isBottomNavigationVisible = visible
    }

    fun setSuppressed(suppressed: Boolean) {
        isSuppressed = suppressed
    }

    fun setDynamicExtraBottomOffset(offsetPx: Int?) {
        dynamicExtraBottomOffsetPx = offsetPx?.coerceAtLeast(0)
    }

    fun resolveBottomInset(defaultInsetPx: Int, productButtonSpacingPx: Int): Int {
        val productButtonHeightPx = dynamicExtraBottomOffsetPx ?: return defaultInsetPx
        return max(defaultInsetPx, productButtonHeightPx + productButtonSpacingPx)
    }

    fun setPresentation(isVisible: Boolean, side: FloatingPromoSide?) {
        presentation = FloatingPromoPresentation(
            isVisible = isVisible,
            side = side ?: presentation.side,
        )
    }

    fun tryConsumeClick(now: Long): Boolean {
        val previousClickAt = lastClickAt
        if (previousClickAt != null && now - previousClickAt < clickDebounceMillis) {
            return false
        }
        lastClickAt = now
        return true
    }

    fun reset() {
        destinationId = null
        isBottomNavigationVisible = true
        isSuppressed = false
        dynamicExtraBottomOffsetPx = null
        lastClickAt = null
        presentation = FloatingPromoPresentation()
    }
}

@Immutable
internal data class FloatingPromoPresentation(
    val isVisible: Boolean = false,
    val side: FloatingPromoSide = FloatingPromoSide.Right,
)
