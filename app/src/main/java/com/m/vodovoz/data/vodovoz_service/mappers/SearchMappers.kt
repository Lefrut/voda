package com.m.vodovoz.data.vodovoz_service.mappers

import com.m.vodovoz.common.model.BaseVodovozAction
import com.m.vodovoz.common.model.DataAllAction
import com.m.vodovoz.common.model.SearchAction
import com.m.vodovoz.data.vodovoz_service.model.MiniSearchRecommendationsDTO
import com.m.vodovoz.data.vodovoz_service.model.ProductsSectionDTO
import com.m.vodovoz.data.vodovoz_service.model.SearchRecommendationsDTO
import com.m.vodovoz.data.vodovoz_service.model.TOVARY_DTO
import com.m.vodovoz.data.vodovoz_service.model.product_details.TOVAR_SECTION_DTO
import com.m.vodovoz.data.vodovoz_service.paging.PagingSourceData
import com.m.vodovoz.domain.general.model.product.ProductModel
import com.m.vodovoz.domain.general.model.product.SearchRecommendationsModel
import com.m.vodovoz.domain.general.model.product.SearchResult
import com.m.vodovoz.domain.general.model.product.SectionModel

private fun ProductsSectionDTO.searchAction(): BaseVodovozAction? =
    when (PEREXOD?.trim()?.lowercase()) {
        "remont" -> DataAllAction.CoolerRepair
        "arenda" -> DataAllAction.CoolerRental
        "sanitarobrabotka" -> DataAllAction.SanitaryMaintenance
        "dostavka" -> DataAllAction.Delivery
        "oplata" -> SearchAction.AboutPayment
        "akction" -> DataAllAction.AllPromotions
        "kontakty" -> SearchAction.Contacts
        "sertifikat" -> DataAllAction.BuyCertificate
        else -> null
    }

internal fun ProductsSectionDTO.toSearchResult(): SearchResult =
    searchAction()?.let { SearchResult.Redirect(it) } ?: SearchResult.Products(toDomain())

internal fun ProductsSectionDTO.toSearchPage(): PagingSourceData<ProductModel> =
    if (searchAction() != null) PagingSourceData(emptyList()) else toPagingSourceData()

fun SearchRecommendationsDTO.toDomain(): SearchRecommendationsModel {
    return SearchRecommendationsModel(
        queries = SLOVA ?: emptyList(),
        section = REKOMEND?.toDomain() ?: throw IllegalArgumentException("SearchRecommendations section can't be null")
    )
}

fun MiniSearchRecommendationsDTO.toDomain(): SearchRecommendationsModel {
    return SearchRecommendationsModel(
        queries = SLOVA ?: emptyList(),
        section = TOVARY?.toDomain() ?: throw IllegalArgumentException("MiniSearchRecommendations section can't be null")
    )
}


fun TOVAR_SECTION_DTO.toDomain(): SectionModel<ProductModel> {
    return SectionModel(
        title = NAME ?: "",
        items = REKOMEND?.mapToDomain() ?: emptyList(),
        button = null
    )
}

fun TOVARY_DTO.toDomain(): SectionModel<ProductModel> {
    return SectionModel(
        title = NAME ?: TITLE ?: "",
        items = (TOVARY ?: DATA)?.mapToDomain() ?: emptyList(),
        button = null
    )
}
