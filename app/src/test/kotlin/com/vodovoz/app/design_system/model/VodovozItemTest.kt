package com.vodovoz.app.design_system.model

import junit.framework.TestCase.assertEquals
import org.junit.Test


class VodovozItemTest {


    @Test
    fun `cart updates product quantities`() {
        val updated = VodovozItemTestData.categories.withUpdatedCartRecursive(
            mapOf(101L to 3, 102L to 5, 201L to 7)
        )

        val apple = updated.findByIdRecursive(101L)!!
        val banana = updated.findByIdRecursive(102L)!!
        val beer = updated.findByIdRecursive(201L)!!

        assertEquals(3, apple.cartQuantity)
        assertEquals(5, banana.cartQuantity)
        assertEquals(7, beer.cartQuantity)
    }

    @Test
    fun `empty cart resets all quantities to 0`() {
        val updated = VodovozItemTestData.categories.withUpdatedCartRecursive(emptyMap())

        updated.flatMap { it.items }.forEach { product ->
            assertEquals(0, product.cartQuantity)
        }
    }

    @Test
    fun `nested categories preserve structure`() {
        val updated = VodovozItemTestData.categories.withUpdatedCartRecursive(
            mapOf(101L to 10, 201L to 20)
        )

        assertEquals(10, updated.findByIdRecursive(101L)!!.cartQuantity)
        assertEquals(20, updated.findByIdRecursive(201L)!!.cartQuantity)
    }


    private fun VodovozItemUi<*>.findByIdRecursive(targetId: Long): VodovozItemUi<*>? {
        if (this.id == targetId) return this

        for (child in items) {
            val found = child.findByIdRecursive(targetId)
            if (found != null) return found
        }
        return null
    }

    @Test
    fun `test withUpdatedFavorites in different cases`() {
        val updated1 = VodovozItemTestData.categories.withUpdatedFavoritesRecursive(
            mapOf(101L to true, 201L to true)
        )
        val updated2 = VodovozItemTestData.categories.withUpdatedFavoritesRecursive(
            emptyMap()
        )
        val updated3 = VodovozItemTestData.categories.withUpdatedFavoritesRecursive(
            mapOf(101L to true, 201L to true, 102L to false)
        )
        val updated4 = VodovozItemTestData.categories.withUpdatedFavoritesRecursive(
            mapOf(101L to true, 201L to true, 102L to true)
        )
        assertEquals(true, updated1.findByIdRecursive(101L)!!.isFavorite)
        assertEquals(true, updated1.findByIdRecursive(201L)!!.isFavorite)

        assertEquals(false, updated2.findByIdRecursive(101L)!!.isFavorite)
        assertEquals(false, updated2.findByIdRecursive(201L)!!.isFavorite)

        assertEquals(true, updated3.findByIdRecursive(101L)!!.isFavorite)
        assertEquals(true, updated3.findByIdRecursive(201L)!!.isFavorite)
        assertEquals(false, updated3.findByIdRecursive(102L)!!.isFavorite)

        assertEquals(true, updated4.findByIdRecursive(101L)!!.isFavorite)
        assertEquals(true, updated4.findByIdRecursive(201L)!!.isFavorite)
        assertEquals(true, updated4.findByIdRecursive(102L)!!.isFavorite)

    }

    private fun Iterable<VodovozItemUi<*>>.findByIdRecursive(targetId: Long): VodovozItemUi<*>? {
        for (item in this) {
            val found = item.findByIdRecursive(targetId)
            if (found != null) return found
        }
        return null
    }

}