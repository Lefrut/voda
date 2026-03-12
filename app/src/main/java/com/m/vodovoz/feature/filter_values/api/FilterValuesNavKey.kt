package com.m.vodovoz.feature.filter_values.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object FilterValuesNavKey : NavKey {
    const val NAV_NAME: String = "feature/filter_values/FilterValues"
}
