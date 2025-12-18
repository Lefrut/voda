package com.m.vodovoz.common.cart

import CoroutineTestBase
import org.junit.Assert.*
import org.junit.Test

class AbstractAppCartTest : CoroutineTestBase() {

    val appCart = AbstractAppCart(dispatcher)


    @Test
    fun testAddOperationType() {

    }


    @Test
    fun testClearOperationType() {

    }


    @Test
    fun testChangeOperationType() {

    }


}


// ======= ТЕСТЫ =======

class OperationMergeTest {

    private fun item(id: Long, q: Int) = CartItem.from(id, q)
    private fun op(type: OperationType, vararg pairs: Pair<Long, Int>) =
        OperationInfo(type = type, items = pairs.associate { (id, q) -> id to item(id, q) })

    @Test
    fun `Add merge - appends when queue empty`() {
        val queue = mutableListOf<OperationInfo>()
        val inc = op(OperationType.Add, 1L to 2)

        OperationType.Add.merge(queue, inc)

        assertEquals(1, queue.size)
        assertEquals(OperationType.Add, queue[0].type)
        assertEquals(mapOf(1L to 2), queue[0].items.mapValues { it.value.quantity })
    }

    @Test
    fun `Add merge - merges into last matching items (sum quantities)`() {
        val queue = mutableListOf(
            op(OperationType.Add, 1L to 2, 2L to 5),
            op(OperationType.Add, 3L to 1)
        )
        val inc = op(OperationType.Add, 1L to 4, 3L to 2)

        OperationType.Add.merge(queue, inc)

        // ожидаем:
        // op0: id=1: 2+4 = 6, id=2 остаётся 5
        // op1: id=3: 1+2 = 3
        assertEquals(2, queue.size)

        assertEquals(mapOf(1L to 6, 2L to 5), queue[0].items.mapValues { it.value.quantity })
        assertEquals(mapOf(3L to 3), queue[1].items.mapValues { it.value.quantity })
    }

    @Test
    fun `Add merge - removes item from existing op when merged quantity becomes zero`() {
        val queue = mutableListOf(
            op(OperationType.Add, 1L to 2)
        )
        val inc = op(OperationType.Add, 1L to -2)

        OperationType.Add.merge(queue, inc)

        // item 1 исчезает, операция становится пустой => удаляется (setOrRemove)
        assertTrue(queue.isEmpty())
    }

    @Test
    fun `Change merge - drops same ids from tail ops then appends Change if no Change after Clear`() {
        val queue = mutableListOf(
            op(OperationType.Add, 1L to 2, 2L to 3),
            op(OperationType.Add, 3L to 1)
        )
        val inc = op(OperationType.Change, 1L to 10)

        OperationType.Change.merge(queue, inc)

        // id=1 удалён из первого Add
        assertEquals(mapOf(2L to 3), queue[0].items.mapValues { it.value.quantity })
        // второй Add не тронут
        assertEquals(mapOf(3L to 1), queue[1].items.mapValues { it.value.quantity })

        // Change добавился третьим
        assertEquals(3, queue.size)
        assertEquals(OperationType.Change, queue[2].type)
        assertEquals(mapOf(1L to 10), queue[2].items.mapValues { it.value.quantity })
    }

    @Test
    fun `Change merge - merges into last Change (after Clear barrier)`() {
        val queue = mutableListOf(
            op(OperationType.Change, 1L to 5),
            op(OperationType.Add, 2L to 1),
            op(OperationType.Change, 3L to 7)
        )
        val inc = op(OperationType.Change, 4L to 9)

        OperationType.Change.merge(queue, inc)

        // последний Change должен быть расширен: + id=4
        assertEquals(3, queue.size)
        assertEquals(OperationType.Change, queue[2].type)
        assertEquals(
            mapOf(3L to 7, 4L to 9),
            queue[2].items.mapValues { it.value.quantity }
        )
    }

    @Test
    fun `Change merge - must not merge into Change BEFORE Clear`() {
        val queue = mutableListOf(
            op(OperationType.Change, 1L to 5),
            op(OperationType.Clear),                 // барьер
            op(OperationType.Add, 2L to 1)
        )
        val inc = op(OperationType.Change, 3L to 7)

        OperationType.Change.merge(queue, inc)

        // Change до Clear не должен быть выбран для merge, должен добавиться новый Change после Clear
        assertEquals(4, queue.size)
        assertEquals(OperationType.Change, queue[0].type) // старый
        assertEquals(OperationType.Clear, queue[1].type)
        assertEquals(OperationType.Add, queue[2].type)
        assertEquals(OperationType.Change, queue[3].type) // новый
    }

    @Test
    fun `Clear merge - clears queue and adds incoming clear op`() {
        val queue = mutableListOf(
            op(OperationType.Add, 1L to 2),
            op(OperationType.Change, 1L to 9)
        )
        val inc = op(OperationType.Clear)

        OperationType.Clear.merge(queue, inc)

        assertEquals(1, queue.size)
        assertEquals(OperationType.Clear, queue[0].type)
        assertTrue(queue[0].items.isEmpty())
    }

}