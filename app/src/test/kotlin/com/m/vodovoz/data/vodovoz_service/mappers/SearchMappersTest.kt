package com.m.vodovoz.data.vodovoz_service.mappers

import com.m.vodovoz.common.model.DataAllAction
import com.m.vodovoz.common.model.SearchAction
import com.m.vodovoz.data.vodovoz_service.model.ProductsSectionDTO
import com.m.vodovoz.data.vodovoz_service.model.VodovozResponseDTO
import com.m.vodovoz.domain.general.model.product.SearchResult
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchMappersTest {
    private val adapter = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
        .adapter<VodovozResponseDTO<ProductsSectionDTO>>(
            Types.newParameterizedType(VodovozResponseDTO::class.java, ProductsSectionDTO::class.java)
        )

    @Test
    fun `recognized codes redirect even without product data`() {
        val actions = mapOf(
            "remont" to DataAllAction.CoolerRepair,
            "arenda" to DataAllAction.CoolerRental,
            "sanitarobrabotka" to DataAllAction.SanitaryMaintenance,
            "dostavka" to DataAllAction.Delivery,
            "oplata" to SearchAction.AboutPayment,
            "akction" to DataAllAction.AllPromotions,
            "kontakty" to SearchAction.Contacts,
            "sertifikat" to DataAllAction.BuyCertificate,
        )

        actions.forEach { (code, action) ->
            val response = parseSearchResponse(
                """{"PEREXOD":"$code","ZAGALOVOK":"Товар не найден"}"""
            )
            assertEquals(code, SearchResult.Redirect(action), response.toSearchResult())
            assertEquals(code, emptyList<Any>(), response.toSearchPage().items)
        }
    }

    @Test
    fun `missing null blank and unknown codes keep product results`() {
        val fields = listOf("", """"PEREXOD":null,""", """"PEREXOD":" ",""", """"PEREXOD":"new-route",""")

        fields.forEach { field ->
            val result = parseSearchResponse(
                """{
                    $field
                    "COUNT":"1",
                    "SORTIROVKA":{
                      "NAMEGLAV":"По популярности",
                      "DANNIESORT":[{"NAME":"По популярности","ZNACHIE":"default","SORT":""}]
                    },
                    "TOVAR":[]
                }"""
            ).toSearchResult() as SearchResult.Products
            assertEquals("1", result.section.productsQuantityText)
            assertEquals("default", result.section.sorting.single().value)
        }
    }

    private fun parseSearchResponse(data: String): ProductsSectionDTO {
        val response = requireNotNull(adapter.fromJson("""{"status":"Success","data":$data}"""))
        return requireNotNull(response.data)
    }
}
