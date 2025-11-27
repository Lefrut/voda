package com.m.vodovoz.design_system.composables.layout

import CoroutineTestBase
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class FixedGridFlowRowDefaultsTest : CoroutineTestBase() {


    @Test
    fun calcItemWidth_allCases() {
        // 1. Простое деление без остатка
        assertEquals(
            55.dp,
            FixedGridFlowRowDefaults.calcItemWidth(
                totalWidth = 110.dp,
                spacing = 0.dp,
                itemsInRow = 2
            )
        )

        // 2. Деление с остатком — проверка floor
        // 111 / 2 = 55.5 → 55
        assertEquals(
            55.dp,
            FixedGridFlowRowDefaults.calcItemWidth(
                totalWidth = 111.dp,
                spacing = 0.dp,
                itemsInRow = 2
            )
        )

        // 3. Учитывается spacing
        // (123 - 2) / 2 = 60.5 → 60
        assertEquals(
            60.dp,
            FixedGridFlowRowDefaults.calcItemWidth(
                totalWidth = 123.dp,
                spacing = 2.dp,
                itemsInRow = 2
            )
        )


        // 5. Маленькая ширина, дробный результат
        // 10 / 3 = 3.33 → 3
        assertEquals(
            3.dp,
            FixedGridFlowRowDefaults.calcItemWidth(
                totalWidth = 10.dp,
                spacing = 0.dp,
                itemsInRow = 3
            )
        )

        assertEquals(
            22.dp,
            FixedGridFlowRowDefaults.calcItemWidth(
                totalWidth = 100.dp,
                spacing = 16.dp,
                itemsInRow = 3
            )
        )

        // Несколько элементов, большой spacing
        // totalSpacing = 16 * 2 = 32
        // (100 - 32) / 3 = 22.66 → floor = 22
        assertEquals(
            22.dp,
            FixedGridFlowRowDefaults.calcItemWidth(
                totalWidth = 100.dp,
                spacing = 16.dp,
                itemsInRow = 3
            )
        )
    }


}