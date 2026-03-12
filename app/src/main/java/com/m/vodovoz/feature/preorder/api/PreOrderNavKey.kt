package com.m.vodovoz.feature.preorder.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object PreOrderNavKey : NavKey {
    const val NAV_NAME: String = "feature/preorder/PreOrder"
}
