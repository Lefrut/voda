package com.m.vodovoz.feature.map.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.m.vodovoz.design_system.model.MapPointUi
import com.m.vodovoz.design_system.model.toDomain
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.domain.general.model.location.MapAddressModel
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
data class MapAddressUi(
    val name: String,
    val city: String,
    val street: String,
    val house: String,
    val point: MapPointUi,
    val fromMoscowToPoint: Float
): Parcelable {
    companion object {
        val Empty = MapAddressUi("", "", "", "", MapPointUi.Empty, 0f)
    }
}

fun MapAddressModel.toUi(): MapAddressUi {
    return MapAddressUi(
        point = point.toUi(),
        name = name,
        city = city,
        street = street,
        house = house,
        fromMoscowToPoint = 0f
    )
}

fun MapAddressUi.toDomain(): MapAddressModel {
    return MapAddressModel(
        point = point.toDomain(),
        name = name,
        city = city,
        street = street,
        house = house,
        fromMoscowToPoint = fromMoscowToPoint.toString()
    )
}


fun List<MapAddressModel>.mapToUi(): List<MapAddressUi> {
    return mapNotNull { address ->
        address.toUi()
    }
}


