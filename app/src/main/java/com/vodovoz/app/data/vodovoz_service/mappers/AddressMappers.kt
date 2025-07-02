package com.vodovoz.app.data.vodovoz_service.mappers

import com.vodovoz.app.data.vodovoz_service.model.address.ADDRESSES_SECTION_DTO
import com.vodovoz.app.data.vodovoz_service.model.address.ADDRESS_ITEM_DTO
import com.vodovoz.app.data.vodovoz_service.model.address.AddAddressDetailsDTO
import com.vodovoz.app.data.vodovoz_service.model.address.AddressesDTO
import com.vodovoz.app.data.vodovoz_service.model.address.MapAreaDTO
import com.vodovoz.app.data.vodovoz_service.model.address.SWITCH_DTO
import com.vodovoz.app.domain.general.model.SwitchModel
import com.vodovoz.app.domain.general.model.location.AddAddressDetailsModel
import com.vodovoz.app.domain.general.model.location.AddressModel
import com.vodovoz.app.domain.general.model.location.MapAreaModel
import com.vodovoz.app.domain.general.model.location.MapPointModel
import com.vodovoz.app.domain.general.model.product.SectionModel

fun MapAreaDTO.toDomain(): MapAreaModel?{
    return MapAreaModel(
        id = ID ?: return null,
        name = TEXT ?: "",
        isMoscowRingRow = MKAD ?: false,
        color = COLOR ?: "",
        points = TOCHKA?.mapNotNull { coordinates ->
            MapPointModel(
                coordinates.getOrNull(0) ?: return@mapNotNull null,
                coordinates.getOrNull(1) ?: return@mapNotNull null
            )
        } ?: return null,
    )
}

fun List<MapAreaDTO>.mapToDomain(): List<MapAreaModel>{
    return mapNotNull { it.toDomain() }.ifEmpty { throw IllegalArgumentException("MapAreas can't be empty") }
}

fun AddressesDTO.toDomain(): List<SectionModel<AddressModel>> {
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

fun AddAddressDetailsDTO.toDomain(): AddAddressDetailsModel {
    return AddAddressDetailsModel(
        addressId = TIP ?: -1,
        addressField = addressField?.toDomain()
            ?: throw IllegalArgumentException("AddAddressDetails field can't be null"),
        gridFields = gridFields?.mapToDomain() ?: emptyList(),
        linearFields = linearFields?.mapToDomain() ?: emptyList(),
        button = KNOPKA?.toDomain()
            ?: throw IllegalArgumentException("AddAddressDetails button can't be null"),
        linearSwitches = switchFields?.mapNotNull { switch ->
            switch.toDomain()
        } ?: emptyList()
    )
}

fun SWITCH_DTO.toDomain(): SwitchModel? {
    return SwitchModel(
        id = PROP_CODE ?: return null,
        name = NAME ?: return null,
        type = TYPE ?: "",
        value = VALUE ?: false
    )
}