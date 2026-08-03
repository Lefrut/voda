package com.m.vodovoz.feature.main

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FloatingPromoUiStateHolderTest {

    private val state = FloatingPromoUiStateHolder(clickDebounceMillis = 800L)

    @Test
    fun `hidden product button resolves to default bottom inset`() {
        state.setDynamicExtraBottomOffset(0)

        assertEquals(
            64,
            state.resolveBottomInset(defaultInsetPx = 64, productButtonSpacingPx = 60),
        )
    }

    @Test
    fun `destination change clears screen-specific offset and suppression`() {
        state.setDynamicExtraBottomOffset(120)
        state.setSuppressed(true)

        state.onDestinationChanged(destinationId = 42)

        assertEquals(42, state.destinationId)
        assertEquals(
            64,
            state.resolveBottomInset(defaultInsetPx = 64, productButtonSpacingPx = 60),
        )
        assertFalse(state.isSuppressed)
    }

    @Test
    fun `visible product button includes configured spacing`() {
        state.setDynamicExtraBottomOffset(80)

        assertEquals(
            140,
            state.resolveBottomInset(defaultInsetPx = 64, productButtonSpacingPx = 60),
        )
    }

    @Test
    fun `presentation stores visibility and server side`() {
        state.setPresentation(isVisible = true, side = FloatingPromoSide.Left)

        assertTrue(state.isPromoVisible)
        assertEquals(FloatingPromoSide.Left, state.presentation.side)
    }

    @Test
    fun `rapid click is consumed only once`() {
        assertTrue(state.tryConsumeClick(now = 1_000L))
        assertFalse(state.tryConsumeClick(now = 1_500L))
        assertTrue(state.tryConsumeClick(now = 1_800L))
    }
}
