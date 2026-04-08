package com.m.vodovoz.feature.catalog.api

import com.m.vodovoz.core.navigation.VodovozNavKey
import kotlinx.serialization.Serializable

@Serializable
data object CatalogNavKey : VodovozNavKey {
    const val NAV_NAME: String = "feature/catalog/Catalog"
}
