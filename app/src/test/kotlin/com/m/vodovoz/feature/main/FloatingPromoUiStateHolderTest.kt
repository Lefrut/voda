package com.m.vodovoz.feature.main

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FloatingPromoUiStateHolderTest {

    private val state = FloatingPromoUiStateHolder(clickDebounceMillis = 800L)

    @Test
    fun `dynamic zero offset overrides static fallback while bottom bar animates out`() {
        state.setDynamicExtraBottomOffset(0)

        assertEquals(0, state.resolveExtraBottomOffset(staticOffsetPx = 72))
    }

    @Test
    fun `destination change clears screen-specific offset and suppression`() {
        state.setDynamicExtraBottomOffset(120)
        state.setSuppressed(true)

        state.onDestinationChanged(destinationId = 42)

        assertEquals(42, state.destinationId)
        assertEquals(72, state.resolveExtraBottomOffset(staticOffsetPx = 72))
        assertFalse(state.isSuppressed)
    }

    @Test
    fun `rapid click is consumed only once`() {
        assertTrue(state.tryConsumeClick(now = 1_000L))
        assertFalse(state.tryConsumeClick(now = 1_500L))
        assertTrue(state.tryConsumeClick(now = 1_800L))
    }
}
