package com.m.vodovoz.feature.search.api

import androidx.navigation3.runtime.NavKey

data class SearchNavKey(
    val query: String = "",
) : NavKey {
    companion object {
        const val NAV_NAME: String = "feature/search/Search"
    }
}
