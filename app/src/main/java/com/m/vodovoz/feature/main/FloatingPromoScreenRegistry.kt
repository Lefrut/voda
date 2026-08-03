package com.m.vodovoz.feature.main

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.m.vodovoz.R

/** The single source of truth for server screen names and destination-specific offsets. */
object FloatingPromoScreenRegistry {

    private val serverScreenToDestinations: Map<String, Set<Int>> = mapOf(
        "main" to setOf(R.id.homeFragment),
        "catalog" to setOf(R.id.catalogFragment),
        "detail" to setOf(R.id.productDetailFragment),
        "detailPromo" to setOf(R.id.promotionDetailFragment),
    )


    fun serverScreenFor(destinationId: Int?): String? {
        if (destinationId == null) return null
        return serverScreenToDestinations.entries
            .firstOrNull { destinationId in it.value }
            ?.key
    }
}
