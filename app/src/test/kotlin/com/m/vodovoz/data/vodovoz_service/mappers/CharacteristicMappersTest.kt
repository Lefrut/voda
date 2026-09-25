package com.m.vodovoz.data.vodovoz_service.mappers

import com.m.vodovoz.data.vodovoz_service.model.product_details.HARAKTERISTIK_BIND_DTO
import com.m.vodovoz.design_system.model.toUi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CharacteristicMappersTest {
    private val url = "https://pub.fsa.gov.ru/rds/declaration/view/17715320/common"

    @Test
    fun `backend link label becomes displayed value and url reaches UI`() {
        val dto = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            .adapter(HARAKTERISTIK_BIND_DTO::class.java)
            .fromJson("""{
                "ID": 1795, "CODE": "SDFKLGDLKMG",
                "NAME": "Документы проверены", "HINT": "Подсказка",
                "VALUE": "$url", "NAMESSILKA": "Посмотреть на сайте"
            }""")!!
        val ui = dto.toDomain()!!.toUi()

        assertEquals("Документы проверены", ui.name)
        assertEquals("Посмотреть на сайте", ui.value)
        assertEquals(url, ui.url)
        assertEquals("Подсказка", ui.hint)
    }

    @Test
    fun `supports label in VALUE and address in NAMESSILKA`() {
        val model = dto("Посмотреть на сайте", url).toDomain()!!
        assertEquals("Посмотреть на сайте", model.value)
        assertEquals(url, model.url)
    }

    @Test
    fun `ordinary characteristics remain plain text`() {
        val model = dto("10 кг", null).toDomain()!!
        assertEquals("10 кг", model.value)
        assertNull(model.url)
    }

    @Test
    fun `invalid address does not create a clickable value`() {
        val model = dto("не ссылка", "Посмотреть на сайте").toDomain()!!
        assertEquals("не ссылка", model.value)
        assertNull(model.url)
    }

    private fun dto(value: String, linkName: String?) = HARAKTERISTIK_BIND_DTO(
        ID = 1, CODE = "", NAME = "Характеристика", HINT = null,
        VALUE = value, NAMESSILKA = linkName
    )
}
