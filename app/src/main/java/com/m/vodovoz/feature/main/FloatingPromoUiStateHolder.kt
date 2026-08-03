package com.m.vodovoz.feature.main

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

    fun resolveExtraBottomOffset(staticOffsetPx: Int): Int {
        return dynamicExtraBottomOffsetPx ?: staticOffsetPx.coerceAtLeast(0)
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
    }
}
