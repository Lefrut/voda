package com.m.vodovoz.feature.catalog.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.design_system.model.BannerUi
import com.m.vodovoz.design_system.model.ParentCategoryUi
import com.m.vodovoz.design_system.model.mapToUi
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.domain.general.model.product.CatalogDetailsModel

@Immutable
data class CatalogDetailsUi(
    val banners: List<BannerUi>,
    val categories: List<ParentCategoryUi>,
)


fun CatalogDetailsModel.toUi(): CatalogDetailsUi {
    return CatalogDetailsUi(
        banners = banners.mapToUi(),
        categories = categories.map { it.toUi() }
    )
}


