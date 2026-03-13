package com.m.vodovoz.data.vodovoz_service.paging

data class PagingSourceData<T>(
    val items: List<T>,
    val pageCount: Int = Int.MAX_VALUE
)
