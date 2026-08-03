package com.m.vodovoz.feature.main

import org.junit.Assert.assertEquals
import org.junit.Test

class FloatingPromoCooldownTest {

    @Test
    fun `cooldown starts with one hour remaining`() {
        assertEquals(
            FLOATING_PROMO_CLICK_COOLDOWN_MS,
            floatingPromoCooldownRemainingMillis(
                clickedAtElapsedRealtime = 1_000L,
                nowElapsedRealtime = 1_000L,
            ),
        )
    }

    @Test
    fun `cooldown returns remaining time before one hour passes`() {
        val halfHourMillis = FLOATING_PROMO_CLICK_COOLDOWN_MS / 2

        assertEquals(
            halfHourMillis,
            floatingPromoCooldownRemainingMillis(
                clickedAtElapsedRealtime = 1_000L,
                nowElapsedRealtime = 1_000L + halfHourMillis,
            ),
        )
    }

    @Test
    fun `cooldown expires after one hour`() {
        assertEquals(
            0L,
            floatingPromoCooldownRemainingMillis(
                clickedAtElapsedRealtime = 1_000L,
                nowElapsedRealtime = 1_000L + FLOATING_PROMO_CLICK_COOLDOWN_MS,
            ),
        )
    }

    @Test
    fun `cooldown remains safe if elapsed time moves backwards`() {
        assertEquals(
            FLOATING_PROMO_CLICK_COOLDOWN_MS,
            floatingPromoCooldownRemainingMillis(
                clickedAtElapsedRealtime = 2_000L,
                nowElapsedRealtime = 1_000L,
            ),
        )
    }
}
