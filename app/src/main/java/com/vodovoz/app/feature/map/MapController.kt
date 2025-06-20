package com.vodovoz.app.feature.map

import android.content.Context
import android.graphics.Color
import com.vodovoz.app.ui.model.DeliveryZoneUI
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingOptions
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.DrivingRouter
import com.yandex.mapkit.directions.driving.DrivingSession
import com.yandex.mapkit.directions.driving.VehicleOptions
import com.yandex.mapkit.geometry.Geo
import com.yandex.mapkit.geometry.LinearRing
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polygon
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.PolylineMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.Error

class MapController(
    private val viewModel: MapFlowViewModel,
    private val context: Context,
) : DrivingSession.DrivingRouteListener {

    private var lastPolyline: PolylineMapObject? = null

    private var drivingSession: DrivingSession? = null

    private val distanceToRouteMap = hashMapOf<Double, DrivingRoute>()

    private val drivingRouter: DrivingRouter by lazy {
        DirectionsFactory.getInstance().createDrivingRouter()
    }

    private lateinit var mapView: MapView

    fun addPolyline(line: Polyline?) {
        lastPolyline?.let {
            try {
                mapView.map.mapObjects.remove(it)
            } catch (_: Throwable) {

            }
        }
        lastPolyline = if (line != null) {
            mapView.map.mapObjects.addPolyline(line)
        } else {
            null
        }
    }

    /**
     * DrivingRouteListener
     */

    override fun onDrivingRoutes(p0: MutableList<DrivingRoute>) {
        for (route in p0) {
            distanceToRouteMap[initRouteLength(route.geometry.points)] = route
        }

        if (distanceToRouteMap.isNotEmpty()) {
            val key = distanceToRouteMap.minOf { it.key }
            val route = distanceToRouteMap[key]
            val polyline = route?.geometry

            val startPoint = route?.requestPoints?.get(0)?.point ?: Point(0.0, 0.0)
            val endPoint = route?.requestPoints?.get(1)?.point ?: Point(0.0, 0.0)
            viewModel.savePolyline(key, polyline, startPoint, endPoint)
        }
    }

    override fun onDrivingRoutesError(p0: Error) {}

    fun submitRequest(start: Point, end: Point) {
        val drivingOptions = DrivingOptions()
        val vehicleOptions = VehicleOptions().apply {

        }
        val requestPoints: ArrayList<RequestPoint> = ArrayList()
        requestPoints.add(
            RequestPoint(
                start,
                RequestPointType.WAYPOINT,
                null,
            )
        )
        requestPoints.add(
            RequestPoint(
                end,
                RequestPointType.WAYPOINT,
                null,
            )
        )
        drivingSession =
            drivingRouter.requestRoutes(requestPoints, drivingOptions, vehicleOptions, this)
    }

    private fun initRouteLength(routes: List<Point>): Double {
        var dis = 0.0

        for (i in 0 until routes.size - 1) {
            val it = routes[i]
            dis += Geo.distance(it, routes[i + 1])
        }
        return dis
    }


    fun drawDeliveryZones(deliveryZoneUIList: List<DeliveryZoneUI>?) {
        runCatching {
            if (deliveryZoneUIList == null) return

            deliveryZoneUIList.forEach { deliveryZoneUI ->
                val zone = mapView.map.mapObjects.addPolygon(
                    Polygon(LinearRing(deliveryZoneUI.pointList), ArrayList())
                )
                zone.fillColor = Color.parseColor(deliveryZoneUI.color)
                zone.strokeWidth = 0.0f
                zone.zIndex = 100.0f
            }
            viewModel.updateZones(true)
        }
    }

}