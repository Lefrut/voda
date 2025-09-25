package com.m.vodovoz.feature.delivery_date.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class DeliveryDateOptionTest {

    @Test
    fun displayDate() {

        val currentDate = LocalDate.of(2023, 2, 21)

        assertEquals(
            "1",
            DeliveryDateOptionUi.Empty.copy(
                value = "21.02.2023"
            ).displayDate("1", "2", currentDate)
        )
        assertEquals(
            "3",
            DeliveryDateOptionUi.Empty.copy(
                value = "22.02.2023"
            ).displayDate("2", "3", currentDate)
        )
        assertEquals(
            "23022023",
            DeliveryDateOptionUi.Empty.copy(
                value = "23022023"
            ).displayDate("5", "6", currentDate)
        )

    }
}