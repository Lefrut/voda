package com.vodovoz.app.feature.map.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.vodovoz.app.design_system.model.MapPointUi
import com.vodovoz.app.design_system.model.toDomain
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.domain.general.model.location.MapAddressModel
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
data class MapAddressUi(
    val name: String,
    val city: String,
    val street: String,
    val house: String,
    val point: MapPointUi,
): Parcelable {
    companion object {
        val Empty = MapAddressUi("", "", "", "", MapPointUi.Empty)
    }
}

fun MapAddressModel.toUi(): MapAddressUi {
    return MapAddressUi(
        point = point.toUi(),
        name = name,
        city = city,
        street = street,
        house = house
    )
}

fun MapAddressUi.toDomain(): MapAddressModel {
    return MapAddressModel(
        point = point.toDomain(),
        name = name,
        city = city,
        street = street,
        house = house
    )
}


fun List<MapAddressModel>.mapToUi(): List<MapAddressUi> {
    return mapNotNull { address ->
        address.toUi()
    }
}


