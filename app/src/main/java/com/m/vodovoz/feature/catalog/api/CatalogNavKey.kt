package com.m.vodovoz.feature.catalog.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object CatalogNavKey : NavKey {
    const val NAV_NAME: String = "feature/catalog/Catalog"
}
