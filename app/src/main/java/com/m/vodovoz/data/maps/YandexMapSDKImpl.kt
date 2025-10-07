package com.m.vodovoz.data.maps

import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingOptions
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.DrivingRouter
import com.yandex.mapkit.directions.driving.DrivingSession
import com.yandex.mapkit.directions.driving.VehicleOptions
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManager
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.Session
import com.yandex.mapkit.search.SuggestItem
import com.yandex.mapkit.search.SuggestOptions
import com.yandex.mapkit.search.SuggestSession
import com.yandex.mapkit.search.SuggestSession.SuggestListener
import com.yandex.mapkit.search.SuggestType
import com.yandex.runtime.Error
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resumeWithException


typealias YandexSearchResponse = Response

@Singleton
class YandexMapSDKImpl @Inject constructor() : YandexMapSDK {

    companion object {
        private const val KILOMETERS = 1.2 /*80km*/

        private val moscowCenter = Point(55.75, 37.62)

        private val moscowBoundingBox = BoundingBox(
            Point(moscowCenter.latitude - KILOMETERS, moscowCenter.longitude - KILOMETERS),
            Point(moscowCenter.latitude + KILOMETERS, moscowCenter.longitude + KILOMETERS)
        )
    }

    private val drivingRouter: DrivingRouter by lazy {
        DirectionsFactory.getInstance().createDrivingRouter()
    }

    private val searchManager: SearchManager by lazy {
        SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)
    }

    private val suggestSession: SuggestSession by lazy {
        searchManager.createSuggestSession()
    }

    private val suggestOptions = SuggestOptions().setSuggestTypes(
        SuggestType.GEO.value
    ).setSuggestWords(false)

    override suspend fun getSuggestsInMoscow(query: String): List<SuggestItem> =
        suspendCancellableCoroutine { cont ->
            suggestSession.suggest(
                query,
                moscowBoundingBox,
                suggestOptions,
                object : SuggestListener {
                    override fun onResponse(items: MutableList<SuggestItem>) {
                        if (cont.isActive) cont.resume(items) { _, _, _ -> }
                    }

                    override fun onError(error: Error) {
                        if (cont.isActive) {
                            cont.resumeWithException(
                                IllegalStateException("Yandex suggest exception")
                            )
                        }
                    }
                }
            )
        }

    override suspend fun searchAddressInMoscow(address: String): YandexSearchResponse {
        return suspendCancellableCoroutine { cont ->
            searchManager.submit(
                address,
                Geometry.fromBoundingBox(moscowBoundingBox),
                SearchOptions(),
                object : Session.SearchListener {
                    override fun onSearchResponse(p0: Response) {
                        cont.resume(p0) { _, _, _ -> }
                    }

                    override fun onSearchError(p0: Error) {
                        cont.resumeWithException(
                            IllegalStateException("Yandex search exception")
                        )
                    }
                }
            )
        }
    }


    override suspend fun getRoutes(start: Point, end: Point): List<List<Point>> {
        return suspendCancellableCoroutine { cont ->
            val requestPoints = listOf(
                RequestPoint(start, RequestPointType.WAYPOINT, null),
                RequestPoint(end, RequestPointType.WAYPOINT, null)
            )

            drivingRouter.requestRoutes(
                requestPoints,
                DrivingOptions(),
                VehicleOptions(),
                object : DrivingSession.DrivingRouteListener {
                    override fun onDrivingRoutes(routes: MutableList<DrivingRoute>) {
                        val route = routes.map { it.geometry.points }.take(10)
                        cont.resume(route) { _, _, _ -> }
                    }

                    override fun onDrivingRoutesError(error: Error) {
                        cont.resumeWithException(IllegalStateException("Driving routes exception"))
                    }
                }
            )

        }
    }


}