package com.m.vodovoz.feature.search.api

import com.m.vodovoz.core.navigation.VodovozNavKey

data class SearchNavKey(
    val query: String = "",
) : VodovozNavKey {
    companion object {
        const val NAV_NAME: String = "feature/search/Search"
    }
}
