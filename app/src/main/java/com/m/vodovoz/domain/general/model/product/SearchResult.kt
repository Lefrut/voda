package com.m.vodovoz.domain.general.model.product

import com.m.vodovoz.common.model.BaseVodovozAction

sealed interface SearchResult {
    data class Products(val section: ProductsSectionModel) : SearchResult
    data class Redirect(val action: BaseVodovozAction) : SearchResult
}
