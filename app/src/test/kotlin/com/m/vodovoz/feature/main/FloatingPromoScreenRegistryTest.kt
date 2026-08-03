package com.m.vodovoz.feature.main

import com.m.vodovoz.R
import org.junit.Assert.assertEquals
import org.junit.Test

class FloatingPromoScreenRegistryTest {

    @Test
    fun `maps supported destinations to canonical server names`() {
        assertEquals("main", FloatingPromoScreenRegistry.serverScreenFor(R.id.homeFragment))
        assertEquals("profile", FloatingPromoScreenRegistry.serverScreenFor(R.id.profileFragment))
        assertEquals("favorites", FloatingPromoScreenRegistry.serverScreenFor(R.id.favoriteFragment))
        assertEquals("catalog", FloatingPromoScreenRegistry.serverScreenFor(R.id.catalogFragment))
        assertEquals("listadressa", FloatingPromoScreenRegistry.serverScreenFor(R.id.addressesFragment))
        assertEquals("history", FloatingPromoScreenRegistry.serverScreenFor(R.id.allOrdersFragment))
        assertEquals("detail", FloatingPromoScreenRegistry.serverScreenFor(R.id.productDetailFragment))
        assertEquals(
            "prodictsList",
            FloatingPromoScreenRegistry.serverScreenFor(R.id.productCatalogFragment),
        )
        assertEquals("promo", FloatingPromoScreenRegistry.serverScreenFor(R.id.allPromotionsFragment))
        assertEquals(
            "detailPromo",
            FloatingPromoScreenRegistry.serverScreenFor(R.id.promotionDetailFragment),
        )
    }

    @Test
    fun `left server list takes priority over right list`() {
        val side = FloatingPromoScreenRegistry.sideFor(
            destinationId = R.id.catalogFragment,
            leftScreenNames = setOf("CATALOG"),
            rightScreenNames = setOf("catalog"),
        )

        assertEquals(FloatingPromoSide.Left, side)
    }

    @Test
    fun `returns right side when destination is only in right server list`() {
        val side = FloatingPromoScreenRegistry.sideFor(
            destinationId = R.id.homeFragment,
            leftScreenNames = emptySet(),
            rightScreenNames = setOf("main"),
        )

        assertEquals(FloatingPromoSide.Right, side)
    }
}
