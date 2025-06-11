package com.vodovoz.app.data.maps

import com.vodovoz.app.R
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.VisibleRegionUtils
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
import kotlin.coroutines.resumeWithException


typealias YandexSearchResponse = Response

class YandexMapSDKImpl : YandexMapSDK {

    companion object{
        private const val KILOMETERS = 1.0

        private val moscowCenter = Point(55.75, 37.62)

        private val suggestBox = BoundingBox(
            Point(moscowCenter.latitude - KILOMETERS, moscowCenter.longitude - KILOMETERS),
            Point(moscowCenter.latitude + KILOMETERS, moscowCenter.longitude + KILOMETERS)
        )
    }

    private val searchManager: SearchManager by lazy {
        SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)
    }

    private val suggestSession: SuggestSession by lazy {
        searchManager.createSuggestSession()
    }

    private val searchOptions = SuggestOptions().setSuggestTypes(
        SuggestType.GEO.value or SuggestType.BIZ.value or SuggestType.TRANSIT.value
    )

    override suspend fun getAddressesInMoscow(query: String): List<SuggestItem> =
        suspendCancellableCoroutine { cont ->
            suggestSession.suggest(
                query,
                suggestBox,
                searchOptions,
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

    override suspend fun getAddressInfo(address: String): YandexSearchResponse {
        TODO("Not yet implemented")
    }

//    override suspend fun getAddressInfo(address: String): YandexSearchResponse {
//        searchManager.submit(
//            address,
//            ,
//            SearchOptions(),
//            object : Session.SearchListener {
//                override fun onSearchResponse(p0: Response) {
//                    val point = Point(
//                        p0.collection.children[0].obj?.geometry?.get(0)?.point?.latitude!!,
//                        p0.collection.children[0].obj?.geometry?.get(0)?.point?.longitude!!
//                    )
//
//
//                }
//
//                override fun onSearchError(p0: Error) {
//
//                }
//            }
//        )
//    }


}