package com.m.vodovoz.feature.favorite.api

import com.m.vodovoz.core.navigation.VodovozNavKey
import kotlinx.serialization.Serializable

@Serializable
data object FavoriteNavKey : VodovozNavKey {
    const val NAV_NAME: String = "feature/favorite/Favorite"
}
