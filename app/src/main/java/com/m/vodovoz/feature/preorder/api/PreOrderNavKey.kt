package com.m.vodovoz.feature.preorder.api

import androidx.navigation3.runtime.NavKey

data class PreOrderNavKey(
    val productId: Long,
) : NavKey {
    companion object {
        const val NAV_NAME: String = "feature/preorder/PreOrder"
    }
}
