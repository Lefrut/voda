package com.m.vodovoz.data.vodovoz_service.model.address


import androidx.annotation.Keep
import com.m.vodovoz.data.vodovoz_service.model.KNOPKA_ORDER_DTO
import com.m.vodovoz.data.vodovoz_service.model.user_data.POLE_DTO
import com.squareup.moshi.Json

@Keep
data class AddAddressDetailsDTO(
    @Json(name = "TIP")
    val id: Long?,
    @Json(name = "METKA")
    val label: ADD_ADDRESS_LABEL_DTO?,
    @Json(name = "addressField")
    val addressField: POLE_DTO?,
    @Json(name = "coordinate")
    val coordinates: COORDINATES_DTO?,
    @Json(name = "distance")
    val fromMKADToAddressKm: Int?,
    @Json(name = "gridFields")
    val gridFields: List<POLE_DTO>?,
    @Json(name = "idzone")
    val zoneId: Int?,
    @Json(name = "switchFields")
    val switchFields: List<SWITCH_DTO>?,
    @Json(name = "linearFields")
    val linearFields: List<POLE_DTO>?,
    @Json(name = "KNOPKA")
    val KNOPKA: KNOPKA_ORDER_DTO?,
)