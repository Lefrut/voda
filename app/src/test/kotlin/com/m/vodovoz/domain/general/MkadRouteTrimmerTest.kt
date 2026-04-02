package com.m.vodovoz.domain.general

import com.m.vodovoz.domain.general.model.location.MapAreaModel
import com.m.vodovoz.domain.general.model.location.MapPointModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MkadRouteTrimmerTest {

    @Test
    fun trimRouteFromAreaBoundary_addsBoundaryIntersection_whenRouteLeavesArea() {
        val area = squareArea()
        val route = listOf(
            MapPointModel(lat = 0.0, lon = 0.0),
            MapPointModel(lat = 0.0, lon = 2.0),
            MapPointModel(lat = 0.0, lon = 3.0)
        )

        val trimmedRoute = MkadRouteTrimmer.trimRouteFromAreaBoundary(
            route = route,
            area = area
        )

        assertEquals(3, trimmedRoute.size)
        assertPoint(trimmedRoute.first(), lat = 0.0, lon = 1.0)
        assertPoint(trimmedRoute[1], lat = 0.0, lon = 2.0)
        assertPoint(trimmedRoute[2], lat = 0.0, lon = 3.0)
    }

    @Test
    fun trimRouteFromAreaBoundary_trimsAtFirstExitSegment_notAtFirstOutsidePoint() {
        val area = squareArea()
        val route = listOf(
            MapPointModel(lat = 0.0, lon = 0.0),
            MapPointModel(lat = 0.0, lon = 0.5),
            MapPointModel(lat = 0.0, lon = 2.0),
            MapPointModel(lat = 0.0, lon = 3.0)
        )

        val trimmedRoute = MkadRouteTrimmer.trimRouteFromAreaBoundary(
            route = route,
            area = area
        )

        assertEquals(3, trimmedRoute.size)
        assertPoint(trimmedRoute.first(), lat = 0.0, lon = 1.0)
        assertPoint(trimmedRoute[1], lat = 0.0, lon = 2.0)
        assertPoint(trimmedRoute[2], lat = 0.0, lon = 3.0)
    }

    @Test
    fun trimRouteFromAreaBoundary_keepsRoute_whenItStartsOnBoundary() {
        val area = squareArea()
        val route = listOf(
            MapPointModel(lat = 0.0, lon = 1.0),
            MapPointModel(lat = 0.0, lon = 2.0)
        )

        val trimmedRoute = MkadRouteTrimmer.trimRouteFromAreaBoundary(
            route = route,
            area = area
        )

        assertEquals(route, trimmedRoute)
    }

    private fun squareArea(): MapAreaModel {
        return MapAreaModel(
            id = MapAreaModel.CORE_AREA_ID,
            name = "Square",
            isMoscowRingRow = true,
            color = "#000000",
            points = listOf(
                MapPointModel(lat = -1.0, lon = -1.0),
                MapPointModel(lat = -1.0, lon = 1.0),
                MapPointModel(lat = 1.0, lon = 1.0),
                MapPointModel(lat = 1.0, lon = -1.0)
            )
        )
    }

    private fun assertPoint(
        point: MapPointModel,
        lat: Double,
        lon: Double,
        epsilon: Double = 1e-9,
    ) {
        assertTrue(kotlin.math.abs(point.lat - lat) <= epsilon)
        assertTrue(kotlin.math.abs(point.lon - lon) <= epsilon)
    }
}
