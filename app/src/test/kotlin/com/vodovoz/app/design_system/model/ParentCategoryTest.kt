package com.vodovoz.app.design_system.model

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test

class ParentCategoryTest {

    private fun category(
        id: Long,
        name: String,
        children: List<ParentCategoryUi> = emptyList()
    ) = ParentCategoryUi(
        id = id,
        name = name,
        picture = "",
        action = null,
        countChildren = children.size,
        childCategories = children
    )

    @Test
    fun `returns parent of only one leaf`() {
        val leaf = category(3, "Leaf")
        val parent = category(2, "Parent", listOf(leaf))
        val root = category(1, "Root", listOf(parent))

        val result = listOf(root).findParentOfOnlyLeaf()

        assertEquals(parent, result)
    }

    @Test
    fun `returns null when multiple leaves exist`() {
        val leaf1 = category(3, "Leaf1")
        val leaf2 = category(4, "Leaf2")
        val parent1 = category(2, "Parent1", listOf(leaf1))
        val parent2 = category(5, "Parent2", listOf(leaf2))
        val root = category(1, "Root", listOf(parent1, parent2))

        val result = listOf(root).findParentOfOnlyLeaf()

        assertNull(result)
    }

    @Test
    fun `returns null when no leaves exist`() {
        val result = emptyList<ParentCategoryUi>().findParentOfOnlyLeaf()

        assertNull(result)
    }

    @Test
    fun `returns null when leaf is root (no parent)`() {
        val leafRoot = category(1, "LeafRoot")

        val result = listOf(leafRoot).findParentOfOnlyLeaf()

        assertNull(result)
    }

    @Test
    fun `returns null when 2 leaf in parent`(){
        val leaf1 = category(3, "Leaf1")
        val leaf2 = category(4, "Leaf2")
        val parent1 = category(2, "Parent1", listOf(leaf1, leaf2))
        val root = category(1, "Root", listOf(parent1))

        val result = listOf(root).findParentOfOnlyLeaf()

        assertNull(result)
    }



}
