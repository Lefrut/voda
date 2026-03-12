package com.m.vodovoz.feature.favorite.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object FavoriteNavKey : NavKey {
    const val NAV_NAME: String = "feature/favorite/Favorite"
}
