package com.m.vodovoz.domain.general

import android.location.Location
import com.m.vodovoz.domain.general.model.location.MapAreaModel
import com.m.vodovoz.domain.general.model.location.MapPointModel
import com.m.vodovoz.domain.general.respository.MapServiceRepository
import com.m.vodovoz.util.extensions.singleGetOrNull
import javax.inject.Inject
import kotlin.math.floor

class MkadDistanceUseCase @Inject constructor(
    private val mapServiceRepository: MapServiceRepository,
) {

    suspend operator fun invoke(
        areas: List<MapAreaModel>,
        addressPoint: MapPointModel,
    ): Result<MkadDistanceResult> {
        val mkadArea = areas.find { area ->
            area.id == MapAreaModel.CORE_AREA_ID && area.isMoscowRingRow
        } ?: return Result.failure(
            IllegalStateException("MKAD area is not available")
        )

        if (mkadArea.contains(addressPoint)) {
            return Result.success(
                MkadDistanceResult(
                    distanceKm = 0f,
                    routeToAddress = emptyList()
                )
            )
        }

        val distanceResult = mkadArea.findNearestPointsTo(target = addressPoint, count = 5)
            .mapNotNull { nearestPoint ->
                val route = mapServiceRepository.getRoutes(
                    start = nearestPoint,
                    end = addressPoint
                ).singleGetOrNull() ?: return@mapNotNull null

                val routeFromMkad = MkadRouteTrimmer.trimRouteFromAreaBoundary(
                    route = route,
                    area = mkadArea
                )

                MkadDistanceResult(
                    distanceKm = routeFromMkad.distanceKm(),
                    routeToAddress = routeFromMkad
                )
            }
            .minByOrNull(MkadDistanceResult::distanceKm)

        return distanceResult?.let(Result.Companion::success)
            ?: Result.failure(IllegalStateException("Unable to calculate MKAD distance"))
    }
}

data class MkadDistanceResult(
    val distanceKm: Float,
    val routeToAddress: List<MapPointModel>,
)

private fun List<MapPointModel>.distanceKm(): Float {
    return zipWithNext { a, b ->
        a.distanceBetween(b).toFloat()
    }.sum() / 1000f
}

private fun MapAreaModel.contains(point: MapPointModel): Boolean {
    val x = point.lon
    val y = point.lat
    var inside = false
    val pts = points
    val n = pts.size

    for (i in 0 until n) {
        val j = (i + n - 1) % n
        val xi = pts[i].lon
        val yi = pts[i].lat
        val xj = pts[j].lon
        val yj = pts[j].lat
        val intersect = ((yi > y) != (yj > y)) &&
            (x < (xj - xi) * (y - yi) / (yj - yi) + xi)

        if (intersect) inside = !inside
    }

    return inside
}

private fun MapAreaModel.findNearestPointsTo(
    target: MapPointModel,
    count: Int = 12,
): List<MapPointModel> {
    return points
        .sortedBy { it.distanceBetween(target) }
        .filterIndexed { index, _ -> index % 2 == 0 }
        .take(count)
}

internal object MkadRouteTrimmer {

    fun trimRouteFromAreaBoundary(
        route: List<MapPointModel>,
        area: MapAreaModel,
    ): List<MapPointModel> {
        if (route.isEmpty()) return route
        if (area.positionOf(route.first()) != AreaPointPosition.Inside) return route

        route.zipWithNext().forEachIndexed { index, (start, end) ->
            val startPosition = area.positionOf(start)
            val endPosition = area.positionOf(end)

            if (startPosition == AreaPointPosition.Inside && endPosition != AreaPointPosition.Inside) {
                val intersectionPoint = area.findBoundaryIntersection(start = start, end = end)
                val routeTail = route.drop(index + 1)

                return when {
                    intersectionPoint == null -> routeTail
                    routeTail.firstOrNull()?.isSameAs(intersectionPoint) == true -> routeTail
                    else -> listOf(intersectionPoint) + routeTail
                }
            }
        }

        return route
    }
}

private enum class AreaPointPosition {
    Inside, Outside, Boundary
}

private fun MapAreaModel.positionOf(point: MapPointModel): AreaPointPosition {
    return when {
        isOnBoundary(point) -> AreaPointPosition.Boundary
        contains(point) -> AreaPointPosition.Inside
        else -> AreaPointPosition.Outside
    }
}

private fun MapAreaModel.isOnBoundary(point: MapPointModel, epsilon: Double = 1e-9): Boolean {
    return edges().any { (start, end) ->
        point.isOnSegment(start = start, end = end, epsilon = epsilon)
    }
}

private fun MapAreaModel.findBoundaryIntersection(
    start: MapPointModel,
    end: MapPointModel,
): MapPointModel? {
    return edges()
        .mapNotNull { (edgeStart, edgeEnd) ->
            segmentIntersection(
                start = start,
                end = end,
                edgeStart = edgeStart,
                edgeEnd = edgeEnd
            )
        }
        .maxByOrNull { intersection ->
            start.projectionFactorTo(end = end, point = intersection)
        }
}

private fun MapAreaModel.edges(): List<Pair<MapPointModel, MapPointModel>> {
    if (points.size < 2) return emptyList()

    return points.zip(points.drop(1) + points.first())
}

private fun MapPointModel.isOnSegment(
    start: MapPointModel,
    end: MapPointModel,
    epsilon: Double,
): Boolean {
    val cross = crossProduct(
        ax = lon - start.lon,
        ay = lat - start.lat,
        bx = end.lon - start.lon,
        by = end.lat - start.lat
    )

    if (kotlin.math.abs(cross) > epsilon) return false

    val minLon = minOf(start.lon, end.lon) - epsilon
    val maxLon = maxOf(start.lon, end.lon) + epsilon
    val minLat = minOf(start.lat, end.lat) - epsilon
    val maxLat = maxOf(start.lat, end.lat) + epsilon

    return lon in minLon..maxLon && lat in minLat..maxLat
}

private fun segmentIntersection(
    start: MapPointModel,
    end: MapPointModel,
    edgeStart: MapPointModel,
    edgeEnd: MapPointModel,
    epsilon: Double = 1e-9,
): MapPointModel? {
    val rX = end.lon - start.lon
    val rY = end.lat - start.lat
    val sX = edgeEnd.lon - edgeStart.lon
    val sY = edgeEnd.lat - edgeStart.lat
    val denominator = crossProduct(rX, rY, sX, sY)

    if (kotlin.math.abs(denominator) <= epsilon) return null

    val qpx = edgeStart.lon - start.lon
    val qpy = edgeStart.lat - start.lat
    val t = crossProduct(qpx, qpy, sX, sY) / denominator
    val u = crossProduct(qpx, qpy, rX, rY) / denominator

    if (t !in -epsilon..(1 + epsilon) || u !in -epsilon..(1 + epsilon)) return null

    return MapPointModel(
        lat = start.lat + t * rY,
        lon = start.lon + t * rX
    )
}

private fun MapPointModel.projectionFactorTo(
    end: MapPointModel,
    point: MapPointModel,
): Double {
    val dx = end.lon - lon
    val dy = end.lat - lat
    val denominator = dx * dx + dy * dy
    if (denominator == 0.0) return 0.0

    return ((point.lon - lon) * dx + (point.lat - lat) * dy) / denominator
}

private fun MapPointModel.isSameAs(other: MapPointModel, epsilon: Double = 1e-9): Boolean {
    return kotlin.math.abs(lat - other.lat) <= epsilon &&
        kotlin.math.abs(lon - other.lon) <= epsilon
}

private fun crossProduct(ax: Double, ay: Double, bx: Double, by: Double): Double {
    return ax * by - ay * bx
}

private fun MapPointModel.distanceBetween(other: MapPointModel): Double {
    val result = FloatArray(1)
    Location.distanceBetween(lat, lon, other.lat, other.lon, result)
    val firstResult = result.getOrNull(0) ?: 0.0f
    return floor(firstResult).toDouble()
}
