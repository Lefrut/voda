package com.m.vodovoz.feature.main

import com.m.vodovoz.R

/** The single source of truth for server screen names and destination-specific offsets. */
object FloatingPromoScreenRegistry {

    private val serverScreenToDestinations: Map<String, Set<Int>> = mapOf(
        "main" to setOf(R.id.homeFragment),
        "profile" to setOf(R.id.profileFragment),
        "favorites" to setOf(R.id.favoriteFragment),
        "catalog" to setOf(R.id.catalogFragment),
        "listadressa" to setOf(R.id.addressesFragment),
        "history" to setOf(R.id.allOrdersFragment),
        "detail" to setOf(R.id.productDetailFragment),
        "prodictsList" to setOf(R.id.productCatalogFragment),
        "promo" to setOf(R.id.allPromotionsFragment),
        "detailPromo" to setOf(R.id.promotionDetailFragment),
    )


    fun serverScreenFor(destinationId: Int?): String? {
        if (destinationId == null) return null
        return serverScreenToDestinations.entries
            .firstOrNull { destinationId in it.value }
            ?.key
    }

    fun sideFor(
        destinationId: Int?,
        leftScreenNames: Set<String>,
        rightScreenNames: Set<String>,
    ): FloatingPromoSide? {
        val serverScreen = serverScreenFor(destinationId) ?: return null
        return when {
            leftScreenNames.containsIgnoringCase(serverScreen) -> FloatingPromoSide.Left
            rightScreenNames.containsIgnoringCase(serverScreen) -> FloatingPromoSide.Right
            else -> null
        }
    }

    private fun Set<String>.containsIgnoringCase(value: String): Boolean {
        return any { it.equals(value, ignoreCase = true) }
    }
}
