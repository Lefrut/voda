package com.m.vodovoz.data.vodovoz_service.mappers

import com.m.vodovoz.common.model.VodovozAction
import com.m.vodovoz.common.model.vodovozActionOf
import com.m.vodovoz.data.vodovoz_service.model.ACTION_DTO
import com.m.vodovoz.data.vodovoz_service.model.FloatingButtonVisibilityDTO
import com.m.vodovoz.data.vodovoz_service.model.FloatingPromoButtonDTO
import com.m.vodovoz.data.vodovoz_service.model.FloatingPromoButtonSizeDTO
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FloatingPromoButtonMappersTest {

    @Test
    fun `parses floating promo endpoint response item`() {
        val json = """
            {
              "DETAIL_PICTURE": "/upload/iblock/310/5ak9i7vo2kmaz0to1msjcm7aa2dh0bya.png",
              "ID": 115060,
              "NAME": "Баннер 1",
              "IBLOCK_ID": 188,
              "HARAKTERISTIK": { "ACTION": "AKCIYA", "ID": 115223 },
              "RAZMER": { "WIDTH": 150, "HEIGHT": 150 },
              "POKAZ": {},
              "OREKLAME": null
            }
        """.trimIndent()
        val dto = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
            .adapter(FloatingPromoButtonDTO::class.java)
            .fromJson(json)
        val model = dto?.toDomain()

        assertEquals("115223", dto?.HARAKTERISTIK?.ID)
        assertEquals(150, model?.width)
        assertEquals(150, model?.height)
        assertEquals(setOf("main"), model?.rightScreenNames)
    }

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
        assertEquals(150, model?.width)
        assertEquals(150, model?.height)
        assertTrue(model?.imageUrl?.endsWith("/promo.png") == true)
    }

    @Test
    fun `empty visibility shows floating promo on main screen`() {
        val model = dto(
            action = "AKCIYA",
            actionId = "115223",
            visibility = FloatingButtonVisibilityDTO(RIGHT = null, LEFT = null),
        ).toDomain()

        assertEquals(emptySet<String>(), model?.leftScreenNames)
        assertEquals(setOf("main"), model?.rightScreenNames)
    }

    @Test
    fun `non-positive dimensions use ui defaults`() {
        val model = dto(
            action = "AKCIYA",
            actionId = "115223",
            size = FloatingPromoButtonSizeDTO(WIDTH = 0, HEIGHT = -1),
        ).toDomain()

        assertNull(model?.width)
        assertNull(model?.height)
    }

    @Test
    fun `known action with invalid id does not produce navigation command`() {
        val model = dto(action = "RAZDEL", actionId = "not-an-id").toDomain()

        assertEquals(null, vodovozActionOf(model?.action, model?.actionId, model?.blockId ?: 0L))
    }

    private fun dto(
        action: String,
        actionId: String,
        visibility: FloatingButtonVisibilityDTO = FloatingButtonVisibilityDTO(
            RIGHT = listOf("catalog", "detail", "detailPromo", "main"),
            LEFT = listOf("profile"),
        ),
        size: FloatingPromoButtonSizeDTO = FloatingPromoButtonSizeDTO(
            WIDTH = 150,
            HEIGHT = 150,
        ),
    ) = FloatingPromoButtonDTO(
        DETAIL_PICTURE = "/promo.png",
        ID = 115060,
        NAME = "Promo",
        IBLOCK_ID = 188,
        HARAKTERISTIK = ACTION_DTO(ACTION = action, ID = actionId),
        POKAZ = visibility,
        RAZMER = size,
        OREKLAME = null,
    )
}
