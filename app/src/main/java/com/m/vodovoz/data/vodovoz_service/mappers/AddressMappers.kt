package com.m.vodovoz.data.vodovoz_service.mappers

import com.m.vodovoz.common.model.VodovozBoolean
import com.m.vodovoz.common.model.boolean
import com.m.vodovoz.common.model.from
import com.m.vodovoz.common.model.toBoleanByVodovoz
import com.m.vodovoz.data.vodovoz_service.di.toVodovozUrl
import com.m.vodovoz.data.vodovoz_service.model.address.ADDRESSES_SECTION_DTO
import com.m.vodovoz.data.vodovoz_service.model.address.ADDRESS_ITEM_DTO
import com.m.vodovoz.data.vodovoz_service.model.address.ADDRESS_METKA_DTO
import com.m.vodovoz.data.vodovoz_service.model.address.ADDRESS_METKA_OKNO_DTO
import com.m.vodovoz.data.vodovoz_service.model.address.ADD_ADDRESS_LABEL_DTO
import com.m.vodovoz.data.vodovoz_service.model.address.AddAddressDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.address.AddressLabelsDTO
import com.m.vodovoz.data.vodovoz_service.model.address.AddressesDTO
import com.m.vodovoz.data.vodovoz_service.model.address.COORDINATES_DTO
import com.m.vodovoz.data.vodovoz_service.model.address.MAP_BUTTON_DTO
import com.m.vodovoz.data.vodovoz_service.model.address.MapAreaDTO
import com.m.vodovoz.data.vodovoz_service.model.address.MapPopupWindowDTO
import com.m.vodovoz.data.vodovoz_service.model.address.MapZonesDTO
import com.m.vodovoz.data.vodovoz_service.model.address.SWITCH_DTO
import com.m.vodovoz.domain.general.model.location.AddAddressDetailsModel
import com.m.vodovoz.domain.general.model.location.AddAddressLabelBSModel
import com.m.vodovoz.domain.general.model.location.AddressLabelModel
import com.m.vodovoz.domain.general.model.location.AddressLabelsModel
import com.m.vodovoz.domain.general.model.location.AddressModel
import com.m.vodovoz.domain.general.model.location.MapAreaModel
import com.m.vodovoz.domain.general.model.location.MapPointModel
import com.m.vodovoz.domain.general.model.location.MapPopupWindowModel
import com.m.vodovoz.domain.general.model.location.MapZonesModel
import com.m.vodovoz.domain.general.model.product.SectionModel
import com.m.vodovoz.domain.general.model.widgets.ImageButtonModel
import com.m.vodovoz.domain.general.model.widgets.SwitchModel
import kotlin.random.Random

fun MapAreaDTO.toDomain(): MapAreaModel? {
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

fun List<MapAreaDTO>.mapToDomain(): List<MapAreaModel> {
    return mapNotNull { it.toDomain() }.ifEmpty { throw IllegalArgumentException("MapAreas can't be empty") }
}

fun MapZonesDTO.toDomain(): MapZonesModel {
    return MapZonesModel(
        areas = ZONE?.mapToDomain() ?: emptyList(),
        imageButton = KNOPKA?.toDomain(),
        popupWindow = KNOPKA?.DATA?.toDomain()
    )
}

fun MapPopupWindowDTO.toDomain(): MapPopupWindowModel {
    return MapPopupWindowModel(
        title = TITLE ?: "",
        description = OPISANIE ?: "",
        items = TOVAR?.mapToDomain() ?: emptyList()
    )
}

fun MAP_BUTTON_DTO.toDomain(): ImageButtonModel {
    return ImageButtonModel(
        name = TEXT ?: "",
        backgroundColor = BACKGROUND ?: "",
        textColor = COLOR ?: "",
        id = Random.nextInt().toString(),
        image = KARTINKA?.toVodovozUrl() ?: ""
    )
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
        address = ADRESS ?: return null,
        otherInfo = PODADRESS ?: ""
    )
}

fun AddAddressDetailsDTO.toDomain(): AddAddressDetailsModel {
    return AddAddressDetailsModel(
        addressId = id ?: -1,
        addressField = addressField?.toDomain()
            ?: throw IllegalArgumentException("AddAddressDetails field can't be null"),
        label = label?.toDomain() ?: AddressLabelModel.Empty,
        formMoscowRingToAddressKm = fromMKADToAddressKm,
        coordinates = coordinates?.toDomain(),
        gridFields = gridFields?.mapToDomain() ?: emptyList(),
        linearFields = linearFields?.mapToDomain() ?: emptyList(),
        button = KNOPKA?.toDomain()
            ?: throw IllegalArgumentException("AddAddressDetails button can't be null"),
        linearSwitches = switchFields?.mapNotNull { switch ->
            switch.toDomain()
        } ?: emptyList()
    )
}

fun ADD_ADDRESS_LABEL_DTO.toDomain(): AddressLabelModel {
    return AddressLabelModel(ID ?: "", NAME ?: "", true)
}

fun AddressLabelsDTO.toDomain(): AddressLabelsModel {
    return AddressLabelsModel(
        labels = METKI?.mapToDomain(ID ?: "") ?: emptyList(),
        popupWindow = OKNO?.toDomain()
    )
}

@JvmName("mapToAddressLabelModelList")
fun List<ADDRESS_METKA_DTO>.mapToDomain(labelId: String): List<AddressLabelModel> {
    return mapNotNull { it.toDomain(labelId) }
}

fun ADDRESS_METKA_OKNO_DTO.toDomain(): AddAddressLabelBSModel? {
    return AddAddressLabelBSModel(
        title = TITLE ?: "",
        hint = TEXT_V_POLE ?: "",
        value = VALUE ?: "",
        button = KNOPKA?.toDomain() ?: return null
    )
}


fun ADDRESS_METKA_DTO.toDomain(labelId: String): AddressLabelModel? {
    return AddressLabelModel(labelId, NAME ?: return null, isEditable ?: false)
}

fun COORDINATES_DTO.toDomain(): MapPointModel? {
    return MapPointModel(lat = latitude ?: return null, lon = longitude ?: return null)
}


@JvmName("mapToSwitchModelList")
fun List<SWITCH_DTO>.mapToDomain(): List<SwitchModel>{
    return mapNotNull { it.toDomain() }
}

//todo
fun SWITCH_DTO.toDomain(): SwitchModel? {
    return SwitchModel(
        id = PROP_CODE ?: ID ?: return null,
        name = NAME.orEmpty(),
        type = TYPE.orEmpty(),
        value = VALUE.toString().toBoleanByVodovoz(),
        enabled = !VodovozBoolean.from(ZABLOCKPOLE).boolean,
        isRequired = OBYAZATELNO.toBoleanByVodovoz()
    )
}