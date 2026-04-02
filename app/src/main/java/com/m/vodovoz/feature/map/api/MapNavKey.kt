package com.m.vodovoz.feature.map.api

import androidx.navigation3.runtime.NavKey
import com.m.vodovoz.core.navigation.viewmodel.SharedViewModelStoreNavKey

data class MapNavKey(
    val addressName: String? = null,
    val source: Source = Source.None,
    override val parentContentKey: String? = null,
) : NavKey, SharedViewModelStoreNavKey {
    override fun toString(): String = parentContentKey
        ?: "MapNavKey(addressName=$addressName, source=$source)"

    enum class Source {
        None,
        AddAddress,
    }

    companion object {
        const val NAV_NAME: String = "feature/map/Map"
    }
}
