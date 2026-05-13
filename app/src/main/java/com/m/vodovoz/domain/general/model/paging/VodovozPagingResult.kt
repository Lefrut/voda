package com.m.vodovoz.domain.general.model.paging

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

data class VodovozPagesInfo(
    val currentPage: Int = 0,
    val pageCount: Int = Int.MAX_VALUE,
    val endOfPaginationReached: Boolean = false,
)

data class VodovozPagingMeta<Meta>(
    val firstPageData: Meta? = null,
    val pagesInfo: VodovozPagesInfo = VodovozPagesInfo(),
)

class VodovozPagingResult<Item : Any, Meta>(
    val flow: Flow<PagingData<Item>>,
    val meta: StateFlow<VodovozPagingMeta<Meta>>,
)
