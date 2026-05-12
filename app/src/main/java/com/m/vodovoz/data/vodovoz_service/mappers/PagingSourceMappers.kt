package com.m.vodovoz.data.vodovoz_service.mappers

import com.m.vodovoz.data.vodovoz_service.model.BrandSectionDTO
import com.m.vodovoz.data.vodovoz_service.model.ProductCommentsDTO
import com.m.vodovoz.data.vodovoz_service.model.PromotionDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.PromotionsDTO
import com.m.vodovoz.data.vodovoz_service.model.ProductsSectionDTO
import com.m.vodovoz.data.vodovoz_service.model.TOVAR_DATA_DTO
import com.m.vodovoz.data.vodovoz_service.model.WaitFeedbackProductsDTO
import com.m.vodovoz.data.vodovoz_service.model.cart.RecommendationsDTO
import com.m.vodovoz.data.vodovoz_service.model.order.OrdersHistoryDetailsDTO
import com.m.vodovoz.data.vodovoz_service.paging.PagingSourceData
import com.m.vodovoz.domain.general.model.order.OrdersHistoryItemModel
import com.m.vodovoz.domain.general.model.product.CommentModel
import com.m.vodovoz.domain.general.model.product.ProductModel
import com.m.vodovoz.domain.general.model.product.WaitFeedbackProductModel
import com.m.vodovoz.domain.general.model.promotion.BrandModel
import com.m.vodovoz.domain.general.model.promotion.PromotionModel

private fun Int?.orUnknownPageCount(): Int = this?.takeIf { it > 0 } ?: Int.MAX_VALUE

fun BrandSectionDTO.toPagingSourceData(): PagingSourceData<BrandModel> {
    return PagingSourceData(
        items = DATA?.mapToDomain()
            ?: throw IllegalArgumentException("Brands can't be null"),
        pageCount = STRANIC.orUnknownPageCount()
    )
}

fun OrdersHistoryDetailsDTO.toPagingSourceData(
    selectedTabId: String?,
): PagingSourceData<OrdersHistoryItemModel> {
    val tabs = (TABS ?: TAB).orEmpty()
    val selectedTab = tabs.firstOrNull { it.ID == selectedTabId }
        ?: tabs.firstOrNull { it.ID == ACTIVE_TAB }
        ?: tabs.firstOrNull()

    if (selectedTab != null) {
        return PagingSourceData(
            items = (selectedTab.ORDERS ?: selectedTab.DANNYE ?: DANNYE).orEmpty().mapToDomain(),
            pageCount = selectedTab.PAGINATION?.totalPages.orUnknownPageCount()
        )
    }

    return PagingSourceData(
        items = DANNYE?.mapToDomain()
            ?: throw IllegalArgumentException("OrderHistory items can't be null")
    )
}

fun ProductCommentsDTO.toPagingSourceData(): PagingSourceData<CommentModel> {
    return PagingSourceData(
        items = COMMENTS.orEmpty().map { it.toDomain() }
    )
}

fun ProductsSectionDTO.toPagingSourceData(): PagingSourceData<ProductModel> {
    return PagingSourceData(
        items = (DATA ?: TOVAR)?.mapToDomain()
            ?: throw IllegalArgumentException("Products can't be null"),
        pageCount = STRANIC.orUnknownPageCount()
    )
}

fun PromotionDetailsDTO.toPagingSourceData(): PagingSourceData<ProductModel> {
    return PagingSourceData(
        items = TOVAR?.DATA?.mapToDomain() ?: emptyList(),
        pageCount = TOVAR?.STRANIC.orUnknownPageCount()
    )
}

fun PromotionsDTO.toPagingSourceData(): PagingSourceData<PromotionModel> {
    return PagingSourceData(
        items = DATA?.mapToDomain() ?: emptyList()
    )
}

fun RecommendationsDTO.toPagingSourceData(): PagingSourceData<ProductModel> {
    return PagingSourceData(
        items = (products ?: data ?: dataLowerCase)!!.mapToDomain(),
        pageCount = (countPages ?: navigation?.totalPages).orUnknownPageCount()
    )
}

fun List<TOVAR_DATA_DTO>.toPagingSourceData(): PagingSourceData<ProductModel> {
    return PagingSourceData(
        items = this.mapToDomain(),
        pageCount = null.orUnknownPageCount()
    )
}


fun WaitFeedbackProductsDTO.toPagingSourceData(): PagingSourceData<WaitFeedbackProductModel> {
    return PagingSourceData(
        items = products!!.mapToDomain()
    )
}
