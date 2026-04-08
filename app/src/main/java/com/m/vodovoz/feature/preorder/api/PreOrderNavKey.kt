package com.m.vodovoz.feature.preorder.api

import com.m.vodovoz.core.navigation.VodovozNavKey

data class PreOrderNavKey(
    val productId: Long,
) : VodovozNavKey {
    companion object {
        const val NAV_NAME: String = "feature/preorder/PreOrder"
    }
}
