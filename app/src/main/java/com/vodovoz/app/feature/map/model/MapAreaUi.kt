package com.vodovoz.app.feature.map.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.vodovoz.app.design_system.model.MapPointUi
import com.vodovoz.app.design_system.model.contains
import com.vodovoz.app.design_system.model.distanceKm
import com.vodovoz.app.design_system.model.mapToUi
import com.vodovoz.app.domain.general.model.location.MapAreaModel
import com.vodovoz.app.ui.graphics.fromHexOrUnspecified
import com.vodovoz.app.ui.yandex_map.distanceBetween

@Immutable
data class MapAreaUi(
    val id: Int,
    val name: String,
    val isMoscowRingRow: Boolean,
    val color: Color,
    val points: List<MapPointUi>,
)

fun List<MapAreaModel>.mapToUi(): List<MapAreaUi> {
    return map { it.toUi() }
}

fun MapAreaModel.toUi(): MapAreaUi {
    return MapAreaUi(
        id = id,
        name = name,
        isMoscowRingRow = isMoscowRingRow,
        color = Color.fromHexOrUnspecified(color),
        points = points.mapToUi()
    )
}


fun MapAreaUi.findNearestPointTo(target: MapPointUi): MapPointUi? {
    return points.minByOrNull { it.distanceBetween(target) }
}