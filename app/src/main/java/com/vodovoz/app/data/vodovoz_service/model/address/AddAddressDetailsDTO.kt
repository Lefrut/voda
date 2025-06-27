package com.vodovoz.app.data.vodovoz_service.model.address


import com.squareup.moshi.Json
import androidx.annotation.Keep
import com.vodovoz.app.data.vodovoz_service.model.KNOPKA_ORDER_DTO
import com.vodovoz.app.data.vodovoz_service.model.user_data.POLE_DTO

@Keep
data class AddAddressDetailsDTO(
    @Json(name = "TIP")
    val TIP: Long?,
    @Json(name = "addressField")
    val addressField: POLE_DTO?,
    @Json(name = "gridFields")
    val gridFields: List<POLE_DTO>?,
    @Json(name = "switchFields")
    val switchFields: List<SWITCH_DTO>?,
    @Json(name = "linearFields")
    val linearFields: List<POLE_DTO>?,
    @Json(name = "KNOPKA")
    val KNOPKA: KNOPKA_ORDER_DTO?
)