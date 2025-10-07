package com.m.vodovoz.data.maps

import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.search.SuggestItem

interface YandexMapSDK {

    suspend fun getSuggestsInMoscow(query: String): List<SuggestItem>

    suspend fun searchAddressInMoscow(address: String): YandexSearchResponse

    suspend fun getRoutes(start: Point, end: Point): List<Point>

}