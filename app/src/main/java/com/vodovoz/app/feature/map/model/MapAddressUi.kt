package com.vodovoz.app.feature.map.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.design_system.model.MapPointUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.domain.general.model.location.MapAddressModel

@Immutable
data class MapAddressUi(
    val name: String,
    val city: String,
    val street: String,
    val house: String,
    val point: MapPointUi,
)

fun MapAddressModel.toUi(): MapAddressUi {
    return MapAddressUi(
        point = point.toUi(),
        name = name,
        city = city,
        street = street,
        house = house
    )
}
