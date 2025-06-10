package com.vodovoz.app.data.vodovoz_service.mappers

import com.vodovoz.app.data.vodovoz_service.model.address.ADDRESSES_SECTION_DTO
import com.vodovoz.app.data.vodovoz_service.model.address.ADDRESS_ITEM_DTO
import com.vodovoz.app.data.vodovoz_service.model.address.AddressesDTO
import com.vodovoz.app.domain.general.model.product.SectionModel
import com.vodovoz.app.domain.general.model.location.AddressModel

fun AddressesDTO.toDomain(): List<SectionModel<AddressModel>>{
    return listOf(FIZLICO?.toDomain(), YRLICO?.toDomain()).mapNotNull { section -> section }
}

fun ADDRESSES_SECTION_DTO.toDomain(): SectionModel<AddressModel> {
    return SectionModel(
        title = NAME ?: "",
        items = DANNYE?.mapNotNull { it.toDomain() } ?: emptyList(),
        button = null
    )
}

fun ADDRESS_ITEM_DTO.toDomain(): AddressModel? {
    return AddressModel(
        id = ID ?: return null,
        personTypeId = PERSON_TYPE_ID ?: -1,
        description = OPISANIE ?: "",
        address = ADRESS ?: return null
    )
}