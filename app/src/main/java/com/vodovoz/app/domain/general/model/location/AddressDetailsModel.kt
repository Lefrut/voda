package com.vodovoz.app.domain.general.model.location

import com.vodovoz.app.common.model.VodovozAddressType
import com.vodovoz.app.common.model.VodovozBoolean

data class AddressDetailsModel(
    val id: Long,
    val name: String,
    val city: String,
    val street: String,
    val type: VodovozAddressType,
    val house: String,
    val flat: String,
    val floor: String,
    val intercom: String,
    val entrance: String,
    val needPass: VodovozBoolean,
    val lat: String,
    val lon: String
)