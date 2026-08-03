package com.m.vodovoz.feature.main

import androidx.compose.ui.unit.dp
import com.m.vodovoz.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FloatingPromoScreenRegistryTest {

    @Test
    fun `maps supported destinations to canonical server names`() {
        assertEquals("main", FloatingPromoScreenRegistry.serverScreenFor(R.id.homeFragment))
        assertEquals("catalog", FloatingPromoScreenRegistry.serverScreenFor(R.id.catalogFragment))
        assertEquals("detail", FloatingPromoScreenRegistry.serverScreenFor(R.id.productDetailFragment))
        assertEquals(
            "detailPromo",
            FloatingPromoScreenRegistry.serverScreenFor(R.id.promotionDetailFragment),
        )
    }
}
