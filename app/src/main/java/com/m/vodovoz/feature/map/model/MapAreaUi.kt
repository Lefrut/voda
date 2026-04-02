package com.m.vodovoz.feature.map.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.m.vodovoz.design_system.model.MapPointUi
import com.m.vodovoz.design_system.model.mapToUi
import com.m.vodovoz.design_system.model.toDomain
import com.m.vodovoz.domain.general.model.location.MapAreaModel
import com.m.vodovoz.ui.graphics.fromHexOrUnspecified
import com.m.vodovoz.ui.yandex_map.distanceBetween

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

fun List<MapAreaUi>.mapToDomain(): List<MapAreaModel> {
    return map { it.toDomain() }
}

fun MapAreaUi.toDomain(): MapAreaModel {
    return MapAreaModel(
        id = id,
        name = name,
        isMoscowRingRow = isMoscowRingRow,
        color = color.toString(),
        points = points.map(MapPointUi::toDomain)
    )
}


fun MapAreaUi.findNearestPointsTo(target: MapPointUi, count: Int = 12): List<MapPointUi> {
    return points
        .sortedBy { it.distanceBetween(target) }
        .filterIndexed { index, _ -> index % 2 == 0 }
        .take(count)
}
