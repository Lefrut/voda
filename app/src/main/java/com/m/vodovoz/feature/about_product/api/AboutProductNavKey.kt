package com.m.vodovoz.feature.about_product.api

import androidx.navigation3.runtime.NavKey
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.model.PriceUi

data class AboutProductNavKey(
    val productId: Long,
    val prices: List<PriceUi>,
    val analogButton: ColorfulButtonUi?,
    val isAvailable: Boolean,
) : NavKey {
    companion object {
        const val NAV_NAME: String = "feature/about_product/AboutProduct"
    }
}
