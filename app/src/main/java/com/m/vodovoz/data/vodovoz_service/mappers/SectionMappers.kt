package com.m.vodovoz.data.vodovoz_service.mappers

import com.m.vodovoz.data.vodovoz_service.model.SUPER_TOP_RAZDEL
import com.m.vodovoz.data.vodovoz_service.model.SUPER_TOP_CATEGORY
import com.m.vodovoz.data.vodovoz_service.model.KNOPKA_DTO
import com.m.vodovoz.data.vodovoz_service.model.KNOPKA_INT_DTO
import com.m.vodovoz.data.vodovoz_service.model.RAZDEL_DTO
import com.m.vodovoz.data.vodovoz_service.model.SuperTopAndBottomSectionsDTO
import com.m.vodovoz.common.model.ButtonAction
import com.m.vodovoz.domain.general.model.product.ButtonModel
import com.m.vodovoz.domain.general.model.product.CategoryWithProductsModel
import com.m.vodovoz.domain.general.model.product.ProductModel
import com.m.vodovoz.domain.general.model.product.SectionModel
import com.m.vodovoz.domain.general.model.product.SuperTopModel

fun SuperTopAndBottomSectionsDTO.toDomain(): SuperTopModel {
    return SuperTopModel(
        topSection = RAZDEL_VERH?.toDomain() ?: SectionModel.empty(),
        bottomSection = RAZDEL_NIZ?.toDomain() ?: SectionModel.empty()
    )
}

fun SUPER_TOP_CATEGORY.toDomain(): CategoryWithProductsModel? {
    return CategoryWithProductsModel(
        id = ID ?: return null,
        products = emptyList(),
        name = NAME ?: return null
    )
}

fun SUPER_TOP_RAZDEL.toDomain(): SectionModel<CategoryWithProductsModel> {
    val categoriesWithProducts = DATA?.mapNotNull { it.toDomain() } ?: emptyList()
    return SectionModel(
        title = NAMERAZDEL ?: "",
        button = KNOPKA?.toDomain(),
        items = categoriesWithProducts
    )
}

fun RAZDEL_DTO.toDomain(): SectionModel<ProductModel> {
    return SectionModel(
        title = TITLE ?: "",
        button = KNOPKA?.toDomain(),
        items = DATA?.mapNotNull { tovarDataDto ->
            tovarDataDto?.toDomain()
        } ?: emptyList()
    )
}

fun KNOPKA_DTO.toDomain(): ButtonModel? {
    return ButtonModel(
        name = NAME ?: "",
        action = ID?.toButtonAction() ?: return null
    )
}

fun KNOPKA_INT_DTO.toDomain(): ButtonModel? {
    return ButtonModel(
        name = NAME ?: "",
        action = ButtonAction.Id(ID ?: return null)
    )
}

private fun String.toButtonAction(): ButtonAction {
    val id = toIntOrNull()
    return if (id != null) {
        ButtonAction.Id(id)
    } else {
        toDataAllAction()
    }
}
