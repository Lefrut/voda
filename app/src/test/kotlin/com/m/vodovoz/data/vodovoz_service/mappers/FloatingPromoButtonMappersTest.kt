package com.m.vodovoz.data.vodovoz_service.mappers

import com.m.vodovoz.common.model.VodovozAction
import com.m.vodovoz.common.model.vodovozActionOf
import com.m.vodovoz.data.vodovoz_service.model.ACTION_DTO
import com.m.vodovoz.data.vodovoz_service.model.FloatingButtonVisibilityDTO
import com.m.vodovoz.data.vodovoz_service.model.FloatingPromoButtonDTO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FloatingPromoButtonMappersTest {

    @Test
    fun `maps server button and validates category id`() {
        val model = dto(action = "RAZDEL", actionId = "5102").toDomain()

        assertEquals(115060, model?.id)
        assertEquals("RAZDEL", model?.action)
        assertEquals("5102", model?.actionId)
        assertEquals(
            VodovozAction.Category(5102),
            vodovozActionOf(model?.action, model?.actionId, model?.blockId ?: 0L),
        )
        assertEquals(setOf("profile"), model?.leftScreenNames)
        assertEquals(setOf("catalog", "detail", "detailPromo", "main"), model?.rightScreenNames)
        assertTrue(model?.imageUrl?.endsWith("/promo.png") == true)
    }

    @Test
    fun `known action with invalid id does not produce navigation command`() {
        val model = dto(action = "RAZDEL", actionId = "not-an-id").toDomain()

        assertEquals(null, vodovozActionOf(model?.action, model?.actionId, model?.blockId ?: 0L))
    }

    private fun dto(action: String, actionId: String) = FloatingPromoButtonDTO(
        DETAIL_PICTURE = "/promo.png",
        ID = 115060,
        NAME = "Promo",
        IBLOCK_ID = 188,
        HARAKTERISTIK = ACTION_DTO(ACTION = action, ID = actionId),
        POKAZ = FloatingButtonVisibilityDTO(
            RIGHT = listOf("catalog", "detail", "detailPromo", "main"),
            LEFT = listOf("profile"),
        ),
        OREKLAME = null,
    )
}
