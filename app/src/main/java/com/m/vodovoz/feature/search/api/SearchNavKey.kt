package com.m.vodovoz.feature.search.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object SearchNavKey : NavKey {
    const val NAV_NAME: String = "feature/search/Search"
}
