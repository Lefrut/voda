package com.vodovoz.app.feature.catalog.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.design_system.model.BannerUi
import com.vodovoz.app.design_system.model.ParentCategoryUi
import com.vodovoz.app.design_system.model.mapToUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.domain.general.model.CatalogDetailsModel

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


